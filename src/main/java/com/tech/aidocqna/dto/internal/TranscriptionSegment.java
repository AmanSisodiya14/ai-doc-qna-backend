package com.tech.aidocqna.dto.internal;

public class TranscriptionSegment {
    private final long start;
    private final long end;
    private final String text;

    public TranscriptionSegment(long start, long end, String text) {
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }
}
