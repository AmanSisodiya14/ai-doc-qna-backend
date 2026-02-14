package com.tech.aidocqna.service;

import com.tech.aidocqna.dto.chat.ChatResponse;
import com.tech.aidocqna.exception.ResourceNotFoundException;
import com.tech.aidocqna.model.Chunk;
import com.tech.aidocqna.model.StoredFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

    private final FileService fileService;
    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearchService;
    private final LLMService llmService;

    public ChatService(
        FileService fileService,
        EmbeddingService embeddingService,
        VectorSearchService vectorSearchService,
        LLMService llmService
    ) {
        this.fileService = fileService;
        this.embeddingService = embeddingService;
        this.vectorSearchService = vectorSearchService;
        this.llmService = llmService;
    }

    @Cacheable(cacheNames = "faq", key = "#fileId.toString() + ':' + #question")
    public ChatResponse ask(String userEmail, Long fileId, String question) {
        log.info("User {} is asking a question about file {}", userEmail, fileId);
        StoredFile file = fileService.getUserFile(fileId, userEmail);
        List<Double> questionEmbedding = embeddingService.generateEmbedding(question);
        List<VectorStoreService.ScoredChunk> topChunks = vectorSearchService.searchTopK(file.getId(), questionEmbedding, 3);
        if (topChunks.isEmpty()) {
            log.warn("No indexed chunks found for file {}", fileId);
            throw new ResourceNotFoundException("No indexed chunks found for file");
        }

        String context = topChunks.stream()
            .map(scored -> scored.chunk().getContent())
            .collect(Collectors.joining("\n---\n"));
        String answer = llmService.generateAnswer(context, question);

        VectorStoreService.ScoredChunk best = topChunks.get(0);
        Chunk bestChunk = best.chunk();
        double confidence = Math.max(0.0, Math.min(1.0, best.score()));
        log.info("CHAT_QUESTION '{}' fileId is '{}'", userEmail, fileId);
        return new ChatResponse(answer, bestChunk.getStartTime(), confidence);
    }

}
