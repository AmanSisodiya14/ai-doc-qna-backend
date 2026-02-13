package com.tech.aidocqna.dto.file;

public class FileSummaryResponse {

    private String summary;

    public FileSummaryResponse() {
    }

    public FileSummaryResponse(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
