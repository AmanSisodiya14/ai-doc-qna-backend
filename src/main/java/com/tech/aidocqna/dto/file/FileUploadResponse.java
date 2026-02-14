package com.tech.aidocqna.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileUploadResponse {
    private Long fileId;
    private String fileName;
    private String fileType;
    private String status;


}
