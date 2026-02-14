package com.tech.aidocqna.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    @NotBlank
    private String storagePath = "uploads";

    @NotNull
    private Long maxFileSizeBytes = 52428800L;

    @Min(500)
    @Max(1000)
    private int chunkSizeTokens = 700;

    @Min(0)
    private int chunkOverlapTokens = 100;

    private final Jwt jwt = new Jwt();
    private final RateLimit rateLimit = new RateLimit();
    private final Groq groq = new Groq();
    private final Embedding embedding = new Embedding();
    private final Transcription transcription = new Transcription();

    @Data
    public static class Jwt {
        @NotBlank
        private String secret;
        private Duration expiration = Duration.ofHours(24);

    }

    @Data
    public static class RateLimit {
        @Min(1)
        private long requestsPerMinute = 60;
    }
    @Data
    public static class Groq {
        @NotBlank
        private String baseUrl;
        @NotBlank
        private String apiKey;
        @NotBlank
        private String model;
    }
    @Data
    public static class Embedding {
        @NotBlank
        private String url;
    }
    @Data
    public static class Transcription {
        @NotBlank
        private String url;
    }
}
