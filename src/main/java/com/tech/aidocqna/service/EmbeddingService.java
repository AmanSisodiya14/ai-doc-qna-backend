package com.tech.aidocqna.service;

import java.util.List;

public interface EmbeddingService {

    List<Double> generateEmbedding(String text);
    List<List<Double>> generateEmbeddings(List<String> texts);
}
