package com.tech.aidocqna.service;

import com.tech.aidocqna.dto.internal.TranscriptionResult;
import com.tech.aidocqna.dto.internal.TranscriptionSegment;
import com.tech.aidocqna.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RemoteTranscriptionService implements TranscriptionService {

    private final WebClient transcriptionWebClient;

    public RemoteTranscriptionService(@Qualifier("transcriptionWebClient") WebClient transcriptionWebClient) {
        this.transcriptionWebClient = transcriptionWebClient;
    }

    @Override
    public TranscriptionResult transcribe(File file) {
        log.info("Transcribing file");
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new FileSystemResource(file));

            Map<String, Object> response = transcriptionWebClient.post()
                .uri("/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null) {
                log.warn("Transcription response is empty");
                throw new ExternalServiceException("Transcription response is empty");
            }

            List<Map<String, Object>> rawSegments = (List<Map<String, Object>>) response.getOrDefault("segments", List.of());
            List<TranscriptionSegment> segments = new ArrayList<>();
            for (Map<String, Object> rawSegment : rawSegments) {
                String segmentText = Objects.toString(rawSegment.getOrDefault("text", ""), "").trim();
                long start = normalizeSeconds(rawSegment.get("start"));
                long end = normalizeSeconds(rawSegment.get("end"));
                segments.add(new TranscriptionSegment(start, end, segmentText));
            }

            String fullText = segments.stream()
                .map(TranscriptionSegment::getText)
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(" "))
                .trim();

            log.info("Transcribed file");
            return new TranscriptionResult(fullText, segments);
        } catch (WebClientResponseException ex) {
            log.error("Failed to call transcription service", ex);
            throw new ExternalServiceException("Transcription service returned status " + ex.getStatusCode().value(), ex);
        } catch (ExternalServiceException ex) {
            log.error("Failed to call transcription service", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to call transcription service", ex);
            throw new ExternalServiceException("Failed to call transcription service", ex);
        }
    }

    private long normalizeSeconds(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return Math.round(number.doubleValue());
        }
        try {
            return Math.round(Double.parseDouble(value.toString()));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
