package com.tech.aidocqna.dto.internal;

import java.util.List;

public class TranscriptionResult {
    private final String text;
    private final List<TranscriptionSegment> segments;

    public TranscriptionResult(String text, List<TranscriptionSegment> segments) {
        this.text = text;
        this.segments = segments;
    }

    public String getText() {
        return text;
    }

    public List<TranscriptionSegment> getSegments() {
        return segments;
    }
}
