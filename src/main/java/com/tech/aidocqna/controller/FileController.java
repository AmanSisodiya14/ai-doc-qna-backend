package com.tech.aidocqna.controller;

import com.tech.aidocqna.dto.ApiResponse;
import com.tech.aidocqna.dto.UserContext;
import com.tech.aidocqna.dto.file.FileMetadataResponse;
import com.tech.aidocqna.dto.file.FileSummaryResponse;
import com.tech.aidocqna.dto.file.FileUploadResponse;
import com.tech.aidocqna.dto.file.TimestampSearchResponse;
import com.tech.aidocqna.service.FileService;
import com.tech.aidocqna.service.SummaryService;
import com.tech.aidocqna.utils.SecurityUtils;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final SummaryService summaryService;

    public FileController(FileService fileService, SummaryService summaryService) {
        this.fileService = fileService;
        this.summaryService = summaryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(@RequestPart("file") MultipartFile file) {

        String email = UserContext.get().getEmail();
        FileUploadResponse response = fileService.upload(email, file);
        return ResponseEntity.ok(ApiResponse.success(response, "Operation successful"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listFiles(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        String email = UserContext.get().getEmail();
        Page<FileMetadataResponse> result = fileService.listFiles(email, page, size);
        Map<String, Object> data = Map.of(
            "content", result.getContent(),
            "page", result.getNumber(),
            "size", result.getSize(),
            "totalElements", result.getTotalElements(),
            "totalPages", result.getTotalPages()
        );
        return ResponseEntity.ok(ApiResponse.success(data, "Operation successful"));
    }

    @GetMapping("/{fileId}/summary")
    public ResponseEntity<ApiResponse<FileSummaryResponse>> summary(@PathVariable Long fileId) {

        String email = UserContext.get().getEmail();
        String summary = summaryService.summarize(email, fileId);
        return ResponseEntity.ok(ApiResponse.success(new FileSummaryResponse(summary), "Operation successful"));
    }

    @GetMapping("/{fileId}/timestamps")
    public ResponseEntity<ApiResponse<TimestampSearchResponse>> timestamps(
        @PathVariable Long fileId,
        @RequestParam @NotBlank String query
    ) {String email = UserContext.get().getEmail();
        TimestampSearchResponse response = fileService.findBestTimestamp(email, fileId, query);
        return ResponseEntity.ok(ApiResponse.success(response, "Operation successful"));
    }
}
