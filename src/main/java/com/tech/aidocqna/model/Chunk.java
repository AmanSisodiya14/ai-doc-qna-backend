package com.tech.aidocqna.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chunks")
@Data

public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private StoredFile file;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Convert(converter = DoubleListJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> embedding;

    @Column
    private Long startTime;

    @Column
    private Long endTime;

    @Column(nullable = false)
    private int chunkOrder;

}
