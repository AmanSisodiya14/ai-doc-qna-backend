package com.tech.aidocqna.controller;

import com.tech.aidocqna.dto.ApiResponse;
import com.tech.aidocqna.dto.UserContext;
import com.tech.aidocqna.dto.chat.ChatRequest;
import com.tech.aidocqna.dto.chat.ChatResponse;
import com.tech.aidocqna.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        String email = UserContext.get().getEmail();
        ChatResponse response = chatService.ask(email, request.getFileId(), request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(response, "Operation successful"));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam UUID fileId, @RequestParam @NotBlank String question) {
        String email = UserContext.get().getEmail();
        return chatService.streamAnswer(email, fileId, question);
    }
}
