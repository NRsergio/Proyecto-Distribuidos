package com.imageprocessing.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de Health Check
 * Verifica que el servidor esté activo
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    
    /**
     * GET /api/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "App-Server is running"));
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthResponse {
        private String status;
        private String message;
    }
}
