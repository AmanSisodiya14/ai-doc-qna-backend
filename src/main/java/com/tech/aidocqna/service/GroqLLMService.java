package com.tech.aidocqna.service;

import com.tech.aidocqna.config.GroqProperties;
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
    private final GroqProperties groqProperties;

    public GroqLLMService(@Qualifier("groqWebClient") WebClient groqWebClient, GroqProperties groqProperties) {
        this.groqWebClient = groqWebClient;
        this.groqProperties = groqProperties;
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
        Map<String, Object> request = Map.of(
            "model", groqProperties.getModel(),
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
                throw new ExternalServiceException("Groq response is empty");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new ExternalServiceException("Groq response choices missing");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = message == null ? null : Objects.toString(message.get("content"), null);
            if (content == null || content.isBlank()) {
                throw new ExternalServiceException("Groq response content missing");
            }
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
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to call Groq chat completion API", ex);
        }
    }
}
