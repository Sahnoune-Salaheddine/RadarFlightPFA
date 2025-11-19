package com.flightradar.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Service pour gérer les opérations JWT (génération et validation)
 * Compatible avec jjwt 0.12.3
 */
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Valider et parser un token JWT
     * Utilise l'API de jjwt 0.12.3
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = getSigningKey();
            // API pour jjwt 0.12.3 - utiliser parser() puis verifyWith()
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Token JWT invalide: " + e.getMessage());
        }
    }
    
    /**
     * Vérifier si un token est valide
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

