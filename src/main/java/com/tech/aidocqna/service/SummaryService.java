package com.tech.aidocqna.service;

import com.tech.aidocqna.model.Chunk;
import com.tech.aidocqna.model.StoredFile;
import com.tech.aidocqna.utils.TokenEstimator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private static final int SMALL_DOC_TOKEN_THRESHOLD = 3500;
    private static final int HIERARCHICAL_BATCH_SIZE = 6;

    private final FileService fileService;
    // OpenAI Client Service (Commented out - using LlamaIndex.ai instead)
     private final OpenAiClientService openAiClientService;
//    private final LlamaIndexClientService llamaIndexClientService;
    private final AuditService auditService;

     public SummaryService(FileService fileService, OpenAiClientService openAiClientService, AuditService auditService) {
         this.fileService = fileService;
         this.openAiClientService = openAiClientService;
         this.auditService = auditService;
     }

//    public SummaryService(FileService fileService, LlamaIndexClientService llamaIndexClientService, AuditService auditService) {
//        this.fileService = fileService;
//        this.llamaIndexClientService = llamaIndexClientService;
//        this.auditService = auditService;
//    }

    @Cacheable(cacheNames = "summary", key = "#fileId.toString()")
    public String summarize(String userEmail, UUID fileId) {
        StoredFile file = fileService.getUserFile(fileId, userEmail);
        List<Chunk> chunks = fileService.getChunks(file.getId());
        String content = chunks.stream().map(Chunk::getContent).collect(Collectors.joining("\n"));
        int tokens = TokenEstimator.estimateTokens(content);
        String summary;
        if (tokens <= SMALL_DOC_TOKEN_THRESHOLD) {
            summary = summarizeBlock(content);
        } else {
            summary = hierarchicalSummary(chunks);
        }
        auditService.logEvent("FILE_SUMMARY", userEmail, "fileId=" + fileId);
        return summary;
    }

    private String summarizeBlock(String content) {
        String system = "Summarize clearly in concise bullet points and one short conclusion.";
        String user = "Summarize this content:\n" + content;
         return openAiClientService.createChatCompletion(system, user);
//        return llamaIndexClientService.createChatCompletion(system, user);
    }

    private String hierarchicalSummary(List<Chunk> chunks) {
        List<String> firstPass = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i += HIERARCHICAL_BATCH_SIZE) {
            int end = Math.min(chunks.size(), i + HIERARCHICAL_BATCH_SIZE);
            String group = chunks.subList(i, end).stream()
                .map(Chunk::getContent)
                .collect(Collectors.joining("\n"));
            firstPass.add(summarizeBlock(group));
        }
        String merged = String.join("\n", firstPass);
        return summarizeBlock(merged);
    }
}
