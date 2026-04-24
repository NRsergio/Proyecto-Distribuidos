package com.imageprocessing.server.controller;

import com.imageprocessing.server.model.dto.AuthResponse;
import com.imageprocessing.server.model.dto.LoginRequest;
import com.imageprocessing.server.model.dto.RegisterRequest;
import com.imageprocessing.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST de Autenticación
 * Endpoints: POST /api/auth/*
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {
    
    private final AuthService authService;
    
    /**
     * POST /api/auth/login
     * Realiza el login de un usuario
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * POST /api/auth/register
     * Registra un nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * GET /api/auth/validar
     * Valida que un token JWT sea válido
     */
    @GetMapping("/validar")
    public ResponseEntity<?> validar(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            boolean valido = authService.validarToken(token);
            
            if (valido) {
                return ResponseEntity.ok(new MessageResponse("Token válido"));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse("Token inválido"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * POST /api/auth/logout
     * Cierra la sesión del usuario
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            authService.logout(token);
            return ResponseEntity.ok(new MessageResponse("Logout exitoso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // DTOs simples para respuestas
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String error;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }
}
