package com.tech.aidocqna.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // OpenAI WebClient (Commented out - using LlamaIndex.ai instead)
     @Bean
     public WebClient openAiWebClient(AppProperties properties) {
         return WebClient.builder()
             .baseUrl(properties.getOpenAi().getBaseUrl())
             .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getOpenAi().getApiKey())
             .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
             .build();
     }

//    @Bean
//    public WebClient llamaIndexWebClient(AppProperties properties) {
//        return WebClient.builder()
//            .baseUrl(properties.getLlamaIndex().getBaseUrl())
//            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getLlamaIndex().getApiKey())
//            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .build();
//    }
}
