package com.tech.aidocqna.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app")
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

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(Long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public int getChunkSizeTokens() {
        return chunkSizeTokens;
    }

    public void setChunkSizeTokens(int chunkSizeTokens) {
        this.chunkSizeTokens = chunkSizeTokens;
    }

    public int getChunkOverlapTokens() {
        return chunkOverlapTokens;
    }

    public void setChunkOverlapTokens(int chunkOverlapTokens) {
        this.chunkOverlapTokens = chunkOverlapTokens;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public static class Jwt {
        @NotBlank
        private String secret;
        private Duration expiration = Duration.ofHours(24);

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getExpiration() {
            return expiration;
        }

        public void setExpiration(Duration expiration) {
            this.expiration = expiration;
        }
    }

    public static class RateLimit {
        @Min(1)
        private long requestsPerMinute = 60;

        public long getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(long requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
    }
}
