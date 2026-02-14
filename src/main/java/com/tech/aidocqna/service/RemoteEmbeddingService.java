package com.tech.aidocqna.service;

import com.tech.aidocqna.exception.BadRequestException;
import com.tech.aidocqna.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RemoteEmbeddingService implements EmbeddingService {

    private final WebClient embeddingWebClient;

    public RemoteEmbeddingService(@Qualifier("embeddingWebClient") WebClient embeddingWebClient) {
        this.embeddingWebClient = embeddingWebClient;
    }

    @Override
    @Cacheable(cacheNames = "embeddings", key = "#text")
    public List<Double> generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new BadRequestException("Text for embedding must not be empty");
        }

        Map<String, String> request = Map.of("text", text.trim());
        try {
            Map<String, Object> response = embeddingWebClient.post()
                .uri("/embed")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null || response.get("embedding") == null) {
                throw new ExternalServiceException("Embedding response is empty");
            }

            List<Number> raw = (List<Number>) response.get("embedding");
            if (raw.isEmpty()) {
                throw new ExternalServiceException("Embedding vector missing");
            }

            return raw.stream().map(Number::doubleValue).toList();
        } catch (WebClientResponseException ex) {
            log.error("Embedding service returned status {}", ex.getStatusCode().value(), ex);
            throw new ExternalServiceException("Embedding service returned status " + ex.getStatusCode().value(), ex);
        } catch (ExternalServiceException ex) {
            log.error("External service error", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to call embedding service", ex);
            throw new ExternalServiceException("Failed to call embedding service", ex);
        }
    }
}
