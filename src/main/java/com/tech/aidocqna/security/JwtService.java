package com.tech.aidocqna.security;

import com.tech.aidocqna.config.AppProperties;
import com.tech.aidocqna.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    private final AppProperties appProperties;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String generateToken(User user) {
        try {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        Instant now = Instant.now();
        Instant expiration = now.plus(appProperties.getJwt().getExpiration());

        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
        } catch (Exception e) {
            log.error("Error generating token for user {}", user.getEmail(), e);
            throw new RuntimeException("Error generating token", e);
        }
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }



    public boolean isTokenValid(String token, String username) {
        String email = extractEmail(token);
        return email.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(appProperties.getJwt().getSecret());
        } catch (IllegalArgumentException e) {
            keyBytes = appProperties.getJwt().getSecret().getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : padKey(keyBytes));
    }

    private byte[] padKey(byte[] source) {
        byte[] out = new byte[32];
        for (int i = 0; i < out.length; i++) {
            out[i] = source[i % source.length];
        }
        return out;
    }
}
