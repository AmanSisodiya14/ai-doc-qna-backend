package com.tech.aidocqna.service;

import com.tech.aidocqna.config.AppProperties;
import com.tech.aidocqna.dto.internal.ChunkPayload;
import com.tech.aidocqna.dto.internal.TranscriptionSegment;
import com.tech.aidocqna.utils.TokenEstimator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
@Slf4j
public class ChunkingService {

    private final AppProperties appProperties;

    public ChunkingService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public List<ChunkPayload> chunkPlainText(String text) {
        log.debug("Chunking plain text");
        String normalized = text == null ? "" : text.trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        List<String> words = List.of(normalized.split("\\s+"));
        int maxTokens = appProperties.getChunkSizeTokens();
        int overlap = Math.min(appProperties.getChunkOverlapTokens(), Math.max(maxTokens - 1, 0));
        int step = Math.max(1, maxTokens - overlap);
        List<ChunkPayload> chunks = new ArrayList<>();

        for (int start = 0; start < words.size(); start += step) {
            int end = Math.min(words.size(), start + maxTokens);
            StringJoiner joiner = new StringJoiner(" ");
            for (int i = start; i < end; i++) {
                joiner.add(words.get(i));
            }
            chunks.add(new ChunkPayload(joiner.toString(), null, null));
            if (end == words.size()) {
                break;
            }
        }
        log.debug("Chunked plain text into {} chunks", chunks.size());
        return chunks;
    }

    public List<ChunkPayload> chunkTranscription(List<TranscriptionSegment> segments) {
        log.debug("Chunking transcription");
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }
        List<ChunkPayload> chunks = new ArrayList<>();
        int maxTokens = appProperties.getChunkSizeTokens();

        StringBuilder builder = new StringBuilder();
        Long currentStart = null;
        Long currentEnd = null;
        int currentTokens = 0;

        for (TranscriptionSegment segment : segments) {
            String segmentText = segment.getText() == null ? "" : segment.getText().trim();
            if (segmentText.isBlank()) {
                continue;
            }
            int segTokens = TokenEstimator.estimateTokens(segmentText);
            if (currentStart == null) {
                currentStart = segment.getStart();
            }
            if (currentTokens + segTokens > maxTokens && currentTokens > 0) {
                chunks.add(new ChunkPayload(builder.toString().trim(), currentStart, currentEnd));
                builder = new StringBuilder();
                currentStart = segment.getStart();
                currentTokens = 0;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(segmentText);
            currentTokens += segTokens;
            currentEnd = segment.getEnd();
        }

        if (!builder.toString().isBlank()) {
            chunks.add(new ChunkPayload(builder.toString().trim(), currentStart, currentEnd));
        }
        log.debug("Chunked transcription into {} chunks", chunks.size());
        return chunks;
    }
}
