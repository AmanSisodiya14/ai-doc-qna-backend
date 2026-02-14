package com.tech.aidocqna.service;

public interface LLMService {

    String generateAnswer(String context, String question);

    String generateSummary(String content);
}
