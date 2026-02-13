package com.tech.aidocqna.repository;

import com.tech.aidocqna.model.StoredFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
    Page<StoredFile> findByUserId(UUID userId, Pageable pageable);
    Optional<StoredFile> findByIdAndUserEmail(UUID id, String email);
}
