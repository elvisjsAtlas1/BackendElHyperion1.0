package com.example.elhyperion.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final Key key;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret:dev-secret}") String secret,
            @Value("${app.jwt.expiration-minutes:120}") long expirationMinutes
    ) {
        this.key = buildKey(secret);
        this.expirationMillis = expirationMinutes * 60_000L;
    }

    private Key buildKey(String secret) {
        try {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) bytes = MessageDigest.getInstance("SHA-256").digest(bytes);
            return Keys.hmacShaKeyFor(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar la clave JWT", e);
        }
    }

    public String generate(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(new Date(now.toEpochMilli() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}