package com.tech.aidocqna.service;

import com.tech.aidocqna.config.AppProperties;
import com.tech.aidocqna.dto.internal.TranscriptionResult;
import com.tech.aidocqna.dto.internal.TranscriptionSegment;
import com.tech.aidocqna.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OpenAiClientService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClientService.class);

    private final WebClient openAiWebClient;
    private final AppProperties appProperties;

    public OpenAiClientService(WebClient openAiWebClient, AppProperties appProperties) {
        this.openAiWebClient = openAiWebClient;
        this.appProperties = appProperties;
    }

    public List<Double> createEmbedding(String text) {
        Map<String, Object> request = Map.of(
            "model", appProperties.getOpenAi().getEmbeddingModel(),
            "input", text
        );

        try {
            Map<String, Object> response = openAiWebClient.post()
                .uri("/v1/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            if (response == null) {
                throw new ExternalServiceException("OpenAI embedding response is empty");
            }
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data == null || data.isEmpty()) {
                throw new ExternalServiceException("OpenAI embedding data is empty");
            }
            List<Number> rawEmbedding = (List<Number>) data.get(0).get("embedding");
            if (rawEmbedding == null) {
                throw new ExternalServiceException("OpenAI embedding vector missing");
            }
            List<Double> out = new ArrayList<>(rawEmbedding.size());
            for (Number number : rawEmbedding) {
                out.add(number.doubleValue());
            }
            return out;
        } catch (Exception ex) {
            log.error("Failed to call OpenAI embeddings API", ex);
            throw new ExternalServiceException("Failed to call OpenAI embeddings API", ex);
        }
    }

    private static long lastCall = 0;

    public synchronized List<List<Double>> embedBatch(List<String> texts) {

        long now = System.currentTimeMillis();
        long diff = now - lastCall;

        if (diff < 25000) {
            try {
                Thread.sleep(25000 - diff);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        lastCall = System.currentTimeMillis();

        return actualEmbeddingCall(texts);
    }

    public List<List<Double>> actualEmbeddingCall(List<String> texts) {

        Map<String, Object> request = Map.of(
                "model", appProperties.getOpenAi().getEmbeddingModel(),
                "input", texts
        );

        Map<String, Object> response = openAiWebClient.post()
                .uri("/v1/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> data =
                (List<Map<String, Object>>) response.get("data");

        List<List<Double>> result = new ArrayList<>();

        for (Map<String, Object> item : data) {
            List<Number> raw = (List<Number>) item.get("embedding");
            result.add(raw.stream()
                    .map(Number::doubleValue)
                    .toList());
        }

        return result;
    }

    public String createChatCompletion(String systemPrompt, String userPrompt) {
        Map<String, Object> request = Map.of(
            "model", appProperties.getOpenAi().getChatModel(),
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ),
            "temperature", 0.2
        );

        try {
            Map<String, Object> response = openAiWebClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            if (response == null) {
                throw new ExternalServiceException("OpenAI chat response is empty");
            }
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new ExternalServiceException("OpenAI chat choices missing");
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = message == null ? null : Objects.toString(message.get("content"), null);
            if (content == null || content.isBlank()) {
                throw new ExternalServiceException("OpenAI chat content missing");
            }
            return content;
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to call OpenAI chat completions API", ex);
        }
    }

    public TranscriptionResult transcribe(Path filePath) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new FileSystemResource(filePath.toFile()));
            builder.part("model", appProperties.getOpenAi().getWhisperModel());
            builder.part("response_format", "verbose_json");

            Map<String, Object> response = openAiWebClient.post()
                .uri("/v1/audio/transcriptions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null) {
                throw new ExternalServiceException("Whisper response is empty");
            }

            String fullText = Objects.toString(response.get("text"), "");
            List<TranscriptionSegment> segments = new ArrayList<>();
            List<Map<String, Object>> rawSegments = (List<Map<String, Object>>) response.get("segments");
            if (rawSegments != null) {
                for (Map<String, Object> raw : rawSegments) {
                    long start = ((Number) raw.getOrDefault("start", 0)).longValue();
                    long end = ((Number) raw.getOrDefault("end", 0)).longValue();
                    String text = Objects.toString(raw.get("text"), "");
                    segments.add(new TranscriptionSegment(start, end, text));
                }
            }
            log.info("Whisper transcription generated with {} segments", segments.size());
            return new TranscriptionResult(fullText, segments);
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to call Whisper transcription API", ex);
        }
    }
}
