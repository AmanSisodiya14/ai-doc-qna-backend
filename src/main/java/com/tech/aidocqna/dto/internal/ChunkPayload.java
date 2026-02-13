package com.tech.aidocqna.dto.internal;

public class ChunkPayload {
    private final String content;
    private final Long startTime;
    private final Long endTime;

    public ChunkPayload(String content, Long startTime, Long endTime) {
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getContent() {
        return content;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }
}
