package com.tech.aidocqna.service;

import com.tech.aidocqna.model.Chunk;
import com.tech.aidocqna.utils.CosineSimilarityUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VectorStoreService {

    private final Map<Long, List<Chunk>> fileIndex = new ConcurrentHashMap<>();

    public void indexChunks(Long fileId, List<Chunk> chunks) {
        fileIndex.put(fileId, new ArrayList<>(chunks));
    }

    public void removeFile(Long fileId) {
        fileIndex.remove(fileId);
    }

    public List<ScoredChunk> search(Long fileId, List<Double> queryEmbedding, int topK) {
        List<Chunk> indexed = fileIndex.getOrDefault(fileId, List.of());
        return indexed.stream()
            .filter(chunk -> chunk.getEmbedding() != null && !chunk.getEmbedding().isEmpty())
            .map(chunk -> new ScoredChunk(chunk, CosineSimilarityUtils.cosineSimilarity(queryEmbedding, chunk.getEmbedding())))
            .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
            .limit(Math.max(1, topK))
            .toList();
    }

    public record ScoredChunk(Chunk chunk, double score) {
    }
}
