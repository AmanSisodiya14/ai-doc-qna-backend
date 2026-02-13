package com.tech.aidocqna.dto.file;

import java.util.UUID;

public class FileUploadResponse {
    private UUID fileId;
    private String fileName;
    private String fileType;
    private String status;

    public FileUploadResponse() {
    }

    public FileUploadResponse(UUID fileId, String fileName, String fileType, String status) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.status = status;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
