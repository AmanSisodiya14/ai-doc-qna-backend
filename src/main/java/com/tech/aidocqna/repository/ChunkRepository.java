package com.tech.aidocqna.repository;

import com.tech.aidocqna.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChunkRepository extends JpaRepository<Chunk, UUID> {
    List<Chunk> findByFileIdOrderByChunkOrderAsc(UUID fileId);
    void deleteByFileId(UUID fileId);
}
