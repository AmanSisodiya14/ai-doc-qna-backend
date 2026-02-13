package com.tech.aidocqna.service;

import com.tech.aidocqna.exception.BadRequestException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    // OpenAI Client (Commented out - using LlamaIndex.ai instead)
     private final OpenAiClientService openAiClientService;

    private final LlamaIndexClientService llamaIndexClientService;

    public EmbeddingService(LlamaIndexClientService llamaIndexClientService, OpenAiClientService openAiClientService) {
        this.llamaIndexClientService = llamaIndexClientService;
        this.openAiClientService = openAiClientService;
    }

    @Cacheable(cacheNames = "embeddings", key = "#text")
    public List<Double> embedText(String text) {
        if (text == null || text.isBlank()) {
            throw new BadRequestException("Text for embedding must not be empty");
        }
         return openAiClientService.createEmbedding(text.trim());
//        return llamaIndexClientService.createEmbedding(text.trim());
    }
    @Cacheable(cacheNames = "embeddings", key = "#text")
    public List<List<Double>> embedText2(List<String> text) {
        if (text == null || text.isEmpty()) {
            throw new BadRequestException("Text for embedding must not be empty");
        }
         return openAiClientService.embedBatch(text);
//        return llamaIndexClientService.embedBatch(text);
    }

}
