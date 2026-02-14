package com.tech.aidocqna.service;

import com.tech.aidocqna.config.AppProperties;
import com.tech.aidocqna.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class GroqLLMService implements LLMService {

    private final WebClient groqWebClient;
    private final AppProperties appProperties;

    public GroqLLMService(@Qualifier("groqWebClient") WebClient groqWebClient, AppProperties appProperties) {
        this.groqWebClient = groqWebClient;
        this.appProperties = appProperties;
    }

    @Override
    public String generateAnswer(String context, String question) {
        String systemPrompt = "You are a helpful assistant for document Q&A. " +
            "Answer only from provided context. If context is insufficient, say you do not know.";
        String userPrompt = "Context:\n" + Objects.toString(context, "") + "\n\nQuestion:\n" + Objects.toString(question, "");
        return generateCompletion(systemPrompt, userPrompt);
    }

    @Override
    public String generateSummary(String content) {
        String systemPrompt = "Summarize clearly in concise bullet points followed by a short conclusion.";
        String userPrompt = "Summarize this content:\n" + Objects.toString(content, "");
        return generateCompletion(systemPrompt, userPrompt);
    }

    private String generateCompletion(String systemPrompt, String userPrompt) {
        log.info("Generating completion ");
        Map<String, Object> request = Map.of(
            "model", appProperties.getGroq().getModel(),
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ),
            "temperature", 0.2
        );

        try {
            Map<String, Object> response = groqWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null) {
                log.warn("Groq response is empty");
                throw new ExternalServiceException("Groq response is empty");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                log.warn("Groq response choices missing");
                throw new ExternalServiceException("Groq response choices missing");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = message == null ? null : Objects.toString(message.get("content"), null);
            if (content == null || content.isBlank()) {
                log.warn("Groq response content missing");
                throw new ExternalServiceException("Groq response content missing");
            }
            log.info("Generated completion");
            return content.trim();
        } catch (WebClientResponseException ex) {
            log.error("Groq error body: {}", ex.getResponseBodyAsString());
            throw new ExternalServiceException(
                    "Groq API returned status " + ex.getStatusCode().value()
                            + " body: " + ex.getResponseBodyAsString(),
                    ex
            );
        }
        catch (ExternalServiceException ex) {
            log.error("External service error", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to call Groq chat completion API", ex);
            throw new ExternalServiceException("Failed to call Groq chat completion API", ex);
        }
    }
}
