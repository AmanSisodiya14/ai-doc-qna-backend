package com.tech.aidocqna.service;

import com.tech.aidocqna.config.AppProperties;
import com.tech.aidocqna.dto.internal.ChunkPayload;
import com.tech.aidocqna.dto.internal.TranscriptionSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

@Service
@Slf4j
public class ChunkingService {

    private static final long MIN_MEDIA_WINDOW_SECONDS = 20L;
    private static final long MAX_MEDIA_WINDOW_SECONDS = 30L;

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

        StringBuilder builder = new StringBuilder();
        Long currentStart = null;
        Long currentEnd = null;
        long previousNormalizedEnd = 0L;

        List<TranscriptionSegment> orderedSegments = segments.stream()
            .sorted(Comparator.comparingLong(TranscriptionSegment::getStart))
            .toList();

        for (TranscriptionSegment segment : orderedSegments) {
            String segmentText = segment.getText() == null ? "" : segment.getText().trim();
            if (segmentText.isBlank()) {
                continue;
            }

            long rawStart = Math.max(0L, segment.getStart());
            long rawEnd = Math.max(rawStart, segment.getEnd());
            long normalizedStart = Math.max(rawStart, previousNormalizedEnd);
            long normalizedEnd = Math.max(normalizedStart, rawEnd);
            previousNormalizedEnd = normalizedEnd;

            if (currentStart == null) {
                currentStart = normalizedStart;
                currentEnd = normalizedEnd;
                builder.append(segmentText);
                continue;
            }

            long currentDuration = Math.max(0L, currentEnd - currentStart);
            long candidateEnd = Math.max(currentEnd, normalizedEnd);
            long candidateDuration = Math.max(0L, candidateEnd - currentStart);

            boolean windowWouldExceedMax = candidateDuration > MAX_MEDIA_WINDOW_SECONDS;
            boolean windowIsReadyToFlush = currentDuration >= MIN_MEDIA_WINDOW_SECONDS;

            if (windowWouldExceedMax && windowIsReadyToFlush) {
                chunks.add(new ChunkPayload(builder.toString().trim(), currentStart, currentEnd));
                builder = new StringBuilder(segmentText);
                currentStart = normalizedStart;
                currentEnd = normalizedEnd;
                continue;
            }

            builder.append(' ').append(segmentText);
            currentEnd = candidateEnd;
        }

        if (!builder.toString().isBlank()) {
            chunks.add(new ChunkPayload(builder.toString().trim(), currentStart, currentEnd));
        }
        log.debug("Chunked transcription into {} chunks", chunks.size());
        return chunks;
    }
}
