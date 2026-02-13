package com.tech.aidocqna.service;

import com.tech.aidocqna.model.Chunk;
import com.tech.aidocqna.repository.ChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VectorSearchService {

    private final ChunkRepository chunkRepository;
    private final VectorStoreService vectorStoreService;

    public VectorSearchService(ChunkRepository chunkRepository, VectorStoreService vectorStoreService) {
        this.chunkRepository = chunkRepository;
        this.vectorStoreService = vectorStoreService;
    }

    public void indexFile(UUID fileId, List<Chunk> chunks) {
        vectorStoreService.indexChunks(fileId, chunks);
    }

    public List<VectorStoreService.ScoredChunk> searchTopK(UUID fileId, List<Double> queryEmbedding, int topK) {
        List<VectorStoreService.ScoredChunk> cached = vectorStoreService.search(fileId, queryEmbedding, topK);
        if (!cached.isEmpty()) {
            return cached;
        }
        List<Chunk> chunks = chunkRepository.findByFileIdOrderByChunkOrderAsc(fileId);
        vectorStoreService.indexChunks(fileId, chunks);
        return vectorStoreService.search(fileId, queryEmbedding, topK);
    }
}
