package com.tech.aidocqna.repository;

import com.tech.aidocqna.model.StoredFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
    Page<StoredFile> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT f FROM StoredFile f WHERE f.id = :id AND f.user.email = :email")
    Optional<StoredFile> findByIdAndUserEmail(UUID id, String email);
}
