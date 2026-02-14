package com.tech.aidocqna.service;

import com.tech.aidocqna.dto.auth.AuthResponse;
import com.tech.aidocqna.dto.auth.LoginRequest;
import com.tech.aidocqna.dto.auth.RegisterRequest;
import com.tech.aidocqna.exception.BadRequestException;
import com.tech.aidocqna.exception.UnauthorizedException;
import com.tech.aidocqna.model.Role;
import com.tech.aidocqna.model.User;
import com.tech.aidocqna.repository.UserRepository;
import com.tech.aidocqna.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email {} already registered", request.getEmail());
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(Role.USER);
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        log.info("Registered new user {}", saved.getEmail());
        return new AuthResponse(saved.getId(), saved.getName(), saved.getEmail(), token);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("User {} is attempting to login", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        // 🔥 Password validation
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user {}", request.getEmail());
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.generateToken(user);
        log.info("User {} logged in successfully", user.getEmail());
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), token);
    }
}
