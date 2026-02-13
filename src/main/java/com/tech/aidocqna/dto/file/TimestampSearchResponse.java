package com.tech.aidocqna.dto.file;

public class TimestampSearchResponse {
    private Long startTime;
    private Long endTime;
    private String excerpt;

    public TimestampSearchResponse() {
    }

    public TimestampSearchResponse(Long startTime, Long endTime, String excerpt) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.excerpt = excerpt;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }
}
