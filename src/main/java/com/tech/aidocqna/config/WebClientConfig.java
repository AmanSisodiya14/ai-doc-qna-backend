package com.tech.aidocqna.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("groqWebClient")
    public WebClient groqWebClient(AppProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getGroq().getBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getGroq().getApiKey())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean("embeddingWebClient")
    public WebClient embeddingWebClient(AppProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getEmbedding().getUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean("transcriptionWebClient")
    public WebClient transcriptionWebClient(AppProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getTranscription().getUrl())
            .build();
    }
}
