package com.imageprocessing.server.controller;

import com.imageprocessing.server.model.dto.BatchRequest;
import com.imageprocessing.server.model.dto.BatchResponse;
import com.imageprocessing.server.model.dto.ProgressResponse;
import com.imageprocessing.server.service.AuthService;
import com.imageprocessing.server.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST de Procesamiento de Lotes
 * Endpoints: POST/GET /api/batch/*
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchRestController {
    
    private final BatchService batchService;
    private final AuthService authService;
    
    /**
     * POST /api/batch/enviar
     * Crea una nueva solicitud de procesamiento de lote
     */
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarLote(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BatchRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            
            // Validar token
            if (!authService.validarToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Token inválido"));
            }
            
            // Obtener usuario del token
            var usuarioOpt = authService.obtenerUsuarioDelToken(token);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Usuario no encontrado"));
            }
            
            // Crear lote
            Long idUsuario = usuarioOpt.get().getIdUsuario();
            BatchResponse response = batchService.crearLote(idUsuario, request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * GET /api/batch/progreso/{idLote}
     * Obtiene el progreso de un lote específico
     */
    @GetMapping("/progreso/{idLote}")
    public ResponseEntity<?> obtenerProgreso(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long idLote) {
        try {
            String token = authHeader.replace("Bearer ", "");
            
            // Validar token
            if (!authService.validarToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Token inválido"));
            }
            
            ProgressResponse response = batchService.obtenerProgreso(idLote);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // DTO para respuesta de error
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String error;
    }
}
