package com.tech.aidocqna.service;

import com.tech.aidocqna.model.Chunk;
import com.tech.aidocqna.repository.ChunkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class VectorSearchService {

    private final ChunkRepository chunkRepository;
    private final VectorStoreService vectorStoreService;

    public VectorSearchService(ChunkRepository chunkRepository, VectorStoreService vectorStoreService) {
        this.chunkRepository = chunkRepository;
        this.vectorStoreService = vectorStoreService;
    }

    public void indexFile(Long fileId, List<Chunk> chunks) {
        vectorStoreService.indexChunks(fileId, chunks);
    }

    public List<VectorStoreService.ScoredChunk> searchTopK(Long fileId, List<Double> queryEmbedding, int topK) {
        log.info("Searching for top {} chunks for file {}", topK, fileId);
        List<VectorStoreService.ScoredChunk> cached = vectorStoreService.search(fileId, queryEmbedding, topK);
        if (!cached.isEmpty()) {
            log.info("Found cached results for file {}", fileId);
            return cached;
        }
        List<Chunk> chunks = chunkRepository.findByFileIdOrderByChunkOrderAsc(fileId);
        vectorStoreService.indexChunks(fileId, chunks);
        log.info("Indexed chunks for file {}", fileId);
        return vectorStoreService.search(fileId, queryEmbedding, topK);
    }
}
