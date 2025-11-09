package ru.streltsov.microserviceapplication.authservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; 
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
        "super-secret-jwt-key-2025-this-key-is-now-long-enough-to-meet-256-bit-requirement-and-contains-at-least-32-bytes".getBytes()
    );

    private final long EXPIRATION_TIME = 15 * 60 * 1000;

    public String generateToken(String email, Long userId) {
        // Создание токена с новым API
        return Jwts.builder()
                .subject(email) 
                .claim("userId", userId)
                .issuedAt(new Date()) 
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) 
                .signWith(SECRET_KEY)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            
            Jwts.parser()
                    .verifyWith(SECRET_KEY) 
                    .build()
                    .parseSignedClaims(token); 
            return true;
        } catch (Exception e) {
            // log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractClaims(String token) {
        // Извлечение полезной нагрузки (claims) с новым API ????
        return Jwts.parser()
                .verifyWith(SECRET_KEY) 
                .build()
                .parseSignedClaims(token)
                .getPayload(); 
    }

}