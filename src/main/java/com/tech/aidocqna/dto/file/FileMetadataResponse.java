package com.tech.aidocqna.dto.file;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class FileMetadataResponse {
    private UUID fileId;
    private String fileName;
    private String fileType;
    private Instant uploadDate;

}
