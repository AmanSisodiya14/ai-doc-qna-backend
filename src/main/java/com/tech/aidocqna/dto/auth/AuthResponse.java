package com.tech.aidocqna.dto.auth;

import java.util.UUID;
import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private Long userId;
    private String name;
    private String email;
    private String token;
}
