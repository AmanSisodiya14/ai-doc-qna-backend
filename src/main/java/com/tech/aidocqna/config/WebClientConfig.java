package com.tech.aidocqna.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("groqWebClient")
    public WebClient groqWebClient(GroqProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean("embeddingWebClient")
    public WebClient embeddingWebClient(EmbeddingProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean("transcriptionWebClient")
    public WebClient transcriptionWebClient(TranscriptionProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getUrl())
            .build();
    }
}
