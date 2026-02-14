package com.tech.aidocqna.service;

import com.tech.aidocqna.model.Chunk;
import com.tech.aidocqna.model.StoredFile;
import com.tech.aidocqna.utils.TokenEstimator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j

public class SummaryService {

    private static final int SMALL_DOC_TOKEN_THRESHOLD = 3500;
    private static final int HIERARCHICAL_BATCH_SIZE = 6;

    private final FileService fileService;
    private final LLMService llmService;

    public SummaryService(FileService fileService, LLMService llmService) {
        this.fileService = fileService;
        this.llmService = llmService;
    }

    @Cacheable(cacheNames = "summary", key = "#fileId.toString()")
    public String summarize(String userEmail, Long fileId) {
        log.info("User {} is generating a summary for file {}", userEmail, fileId);
        StoredFile file = fileService.getUserFile(fileId, userEmail);
        List<Chunk> chunks = fileService.getChunks(file.getId());
        String content = chunks.stream().map(Chunk::getContent).collect(Collectors.joining("\n"));
        int tokens = TokenEstimator.estimateTokens(content);

        String summary = tokens <= SMALL_DOC_TOKEN_THRESHOLD
            ? llmService.generateSummary(content)
            : hierarchicalSummary(chunks);

        log.info("Generated summary for file {}", fileId);
        return summary;
    }

    private String hierarchicalSummary(List<Chunk> chunks) {
        List<String> firstPass = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i += HIERARCHICAL_BATCH_SIZE) {
            int end = Math.min(chunks.size(), i + HIERARCHICAL_BATCH_SIZE);
            String group = chunks.subList(i, end).stream()
                .map(Chunk::getContent)
                .collect(Collectors.joining("\n"));
            firstPass.add(llmService.generateSummary(group));
        }
        String merged = String.join("\n", firstPass);
        return llmService.generateSummary(merged);
    }
}
