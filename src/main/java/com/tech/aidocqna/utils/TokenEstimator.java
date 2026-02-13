package com.tech.aidocqna.utils;

public final class TokenEstimator {

    private TokenEstimator() {
    }

    public static int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
