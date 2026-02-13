package com.tech.aidocqna.service;

import com.tech.aidocqna.dto.file.FileMetadataResponse;
import com.tech.aidocqna.dto.file.FileUploadResponse;
import com.tech.aidocqna.dto.file.TimestampSearchResponse;
import com.tech.aidocqna.dto.internal.ChunkPayload;
import com.tech.aidocqna.dto.internal.TranscriptionResult;
import com.tech.aidocqna.exception.ResourceNotFoundException;
import com.tech.aidocqna.model.Chunk;
import com.tech.aidocqna.model.StoredFile;
import com.tech.aidocqna.model.User;
import com.tech.aidocqna.repository.ChunkRepository;
import com.tech.aidocqna.repository.StoredFileRepository;
import com.tech.aidocqna.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final UserRepository userRepository;
    private final StoredFileRepository storedFileRepository;
    private final ChunkRepository chunkRepository;
    private final FileStorageService fileStorageService;
    private final PdfExtractionService pdfExtractionService;
    private final TranscriptionService transcriptionService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearchService;
    private final AuditService auditService;

    public FileService(
        UserRepository userRepository,
        StoredFileRepository storedFileRepository,
        ChunkRepository chunkRepository,
        FileStorageService fileStorageService,
        PdfExtractionService pdfExtractionService,
        TranscriptionService transcriptionService,
        ChunkingService chunkingService,
        EmbeddingService embeddingService,
        VectorSearchService vectorSearchService,
        AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.storedFileRepository = storedFileRepository;
        this.chunkRepository = chunkRepository;
        this.fileStorageService = fileStorageService;
        this.pdfExtractionService = pdfExtractionService;
        this.transcriptionService = transcriptionService;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.vectorSearchService = vectorSearchService;
        this.auditService = auditService;
    }

    @Transactional
    public FileUploadResponse upload(String userEmail, MultipartFile multipartFile) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Path storedPath = fileStorageService.store(multipartFile);
        String extension = fileStorageService.extensionOf(multipartFile.getOriginalFilename());

        StoredFile file = new StoredFile();
        file.setUser(user);
        file.setFileName(multipartFile.getOriginalFilename());
        file.setFileType(extension);
        file.setStoragePath(storedPath.toString());

        List<ChunkPayload> chunkPayloads;
        if ("pdf".equals(extension)) {
            String text = pdfExtractionService.extractText(storedPath);
            file.setExtractedText(text);
            chunkPayloads = chunkingService.chunkPlainText(text);
        } else {
            TranscriptionResult transcription = transcriptionService.transcribeAsync(storedPath).join();
            file.setTranscript(transcription.getText());
            file.setExtractedText(transcription.getText());
            chunkPayloads = chunkingService.chunkTranscription(transcription.getSegments());
        }

        StoredFile saved = storedFileRepository.save(file);
        List<Chunk> chunks = persistChunks(saved, chunkPayloads);
        vectorSearchService.indexFile(saved.getId(), chunks);
        auditService.logEvent("FILE_UPLOADED", userEmail, "fileId=" + saved.getId());
        log.info("Uploaded file {} with {} chunks", saved.getId(), chunks.size());

        return new FileUploadResponse(saved.getId(), saved.getFileName(), saved.getFileType(), "PROCESSED");
    }

    public Page<FileMetadataResponse> listFiles(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)));
        return storedFileRepository.findByUserId(user.getId(), pageable)
            .map(file -> new FileMetadataResponse(file.getId(), file.getFileName(), file.getFileType(), file.getUploadDate()));
    }

    public StoredFile getUserFile(UUID fileId, String userEmail) {
        return storedFileRepository.findByIdAndUserEmail(fileId, userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    public List<Chunk> getChunks(UUID fileId) {
        return chunkRepository.findByFileIdOrderByChunkOrderAsc(fileId);
    }

    public TimestampSearchResponse findBestTimestamp(String userEmail, UUID fileId, String query) {
        getUserFile(fileId, userEmail);
        List<Double> queryEmbedding = embeddingService.embedText(query);
        List<VectorStoreService.ScoredChunk> matches = vectorSearchService.searchTopK(fileId, queryEmbedding, 1);
        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("No matching chunk found for query");
        }
        Chunk best = matches.get(0).chunk();
        return new TimestampSearchResponse(best.getStartTime(), best.getEndTime(), best.getContent());
    }

    private List<Chunk> persistChunks(StoredFile file, List<ChunkPayload> chunkPayloads) {

        // 1️⃣ Extract all chunk texts
        List<String> contents = chunkPayloads.stream()
                .map(ChunkPayload::getContent)
                .toList();

        // 2️⃣ Call OpenAI ONCE (batch embedding)
        List<List<Double>> embeddings = embeddingService.embedText2(contents);

        // 3️⃣ Build chunk entities
        List<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkPayloads.size(); i++) {
            ChunkPayload payload = chunkPayloads.get(i);

            Chunk chunk = new Chunk();
            chunk.setFile(file);
            chunk.setContent(payload.getContent());
            chunk.setStartTime(payload.getStartTime());
            chunk.setEndTime(payload.getEndTime());
            chunk.setChunkOrder(i);
            chunk.setEmbedding(embeddings.get(i));

            chunks.add(chunk);
        }

        return chunkRepository.saveAll(chunks);
    }

}
