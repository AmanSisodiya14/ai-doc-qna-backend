package com.tech.aidocqna.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class ChatRequest {
    @NotNull
    private Long fileId;
    @NotBlank
    private String message;


}
