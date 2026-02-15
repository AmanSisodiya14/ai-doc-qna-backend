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
        log.info("Generating embedding for text");
        if (text == null || text.isBlank()) {
            log.warn("Empty text for embedding");
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
                log.warn("Empty embedding response");
                throw new ExternalServiceException("Embedding response is empty");
            }

            List<Number> raw = (List<Number>) response.get("embedding");
            if (raw.isEmpty()) {
                log.warn("Empty embedding vector");
                throw new ExternalServiceException("Embedding vector missing");
            }

            log.info("Generated embedding for text");
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

    @Override
    @Cacheable(cacheNames = "embeddings", key = "#texts")
    public List<List<Double>> generateEmbeddings(List<String> texts) {
    log.info("Generating embeddings for {} texts", texts.size());
        if (texts == null || texts.isEmpty()) {
            throw new BadRequestException("Texts must not be empty");
        }

        try {

            Map<String, Object> request = Map.of("texts", texts);

            Map<String, Object> response = embeddingWebClient.post()
                    .uri("/embed-batch")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("embeddings") == null) {
                throw new ExternalServiceException("Empty embedding response");
            }

            List<List<Number>> raw =
                    (List<List<Number>>) response.get("embeddings");

            log.info("Generated embeddings for {} texts", texts.size());
            return raw.stream()
                    .map(list -> list.stream()
                            .map(Number::doubleValue)
                            .toList())
                    .toList();

        } catch (WebClientResponseException ex) {
            log.error("Embedding service returned status {}", ex.getStatusCode().value(), ex);
            throw new ExternalServiceException(
                    "Embedding service error: " + ex.getStatusCode().value(), ex);

        } catch (Exception ex) {
            log.error("Failed to call embedding service", ex);
            throw new ExternalServiceException(
                    "Failed to call embedding service", ex);
        }
    }

}
