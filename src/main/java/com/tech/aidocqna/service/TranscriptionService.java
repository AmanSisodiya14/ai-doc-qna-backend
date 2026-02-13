package com.tech.aidocqna.service;

import com.tech.aidocqna.dto.internal.TranscriptionResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Service
public class TranscriptionService {

    // OpenAI Client Service (Commented out - using LlamaIndex.ai instead)
     private final OpenAiClientService openAiClientService;
//    private final LlamaIndexClientService llamaIndexClientService;

     public TranscriptionService(OpenAiClientService openAiClientService) {
         this.openAiClientService = openAiClientService;
     }

//    public TranscriptionService(LlamaIndexClientService llamaIndexClientService) {
//        this.llamaIndexClientService = llamaIndexClientService;
//    }

    @Async("transcriptionExecutor")
    public CompletableFuture<TranscriptionResult> transcribeAsync(Path filePath) {
         return CompletableFuture.completedFuture(openAiClientService.transcribe(filePath));
//        return CompletableFuture.completedFuture(llamaIndexClientService.transcribe(filePath));
    }
}
