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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        auditService.logEvent("USER_REGISTERED", saved.getEmail(), "registration successful");
        log.info("Registered new user {}", saved.getEmail());
        return new AuthResponse(saved.getId(), saved.getEmail(), token);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        // 🔥 Password validation
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.generateToken(user);
        auditService.logEvent("USER_LOGIN", user.getEmail(), "login successful");
        return new AuthResponse(user.getId(), user.getEmail(), token);
    }
}
