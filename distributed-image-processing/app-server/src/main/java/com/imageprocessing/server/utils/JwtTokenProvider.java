package com.imageprocessing.server.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Proveedor de utilidades para generación y validación de JWT
 * Responsable de crear tokens para autenticación
 */
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret:mySecretKeyForImageProcessingDistributedSystem123456789}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Genera un JWT para el usuario
     * @param username nombre de usuario
     * @param userId ID del usuario
     * @return token JWT
     */
    public String generateToken(String username, String userId) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Extrae el nombre de usuario del token JWT
     * @param token token JWT
     * @return nombre de usuario
     */
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Valida la firma y expiración del JWT
     * @param token token JWT
     * @return true si es válido, false si no
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
