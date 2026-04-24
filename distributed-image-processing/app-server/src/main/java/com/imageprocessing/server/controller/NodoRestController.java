package com.imageprocessing.server.controller;

import com.imageprocessing.server.service.NodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de nodos
 * Endpoints: POST/GET /api/nodos/*
 */
@RestController
@RequestMapping("/api/nodos")
@RequiredArgsConstructor
public class NodoRestController {
    
    private final NodoService nodoService;
    
    /**
     * POST /api/nodos/registrar
     * Registra un nuevo nodo trabajador
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarNodo(@RequestBody RegistroNodoRequest request) {
        try {
            var nodo = nodoService.registrarNodo(
                request.getNombre(),
                request.getHost(),
                request.getPuertoRmi()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(nodo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * GET /api/nodos/activos
     * Obtiene todos los nodos conectados
     */
    @GetMapping("/activos")
    public ResponseEntity<?> obtenerNodosActivos() {
        try {
            var nodos = nodoService.obtenerNodosActivos();
            return ResponseEntity.ok(nodos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * PUT /api/nodos/{idNodo}/estado
     * Actualiza el estado de un nodo
     */
    @PutMapping("/{idNodo}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long idNodo,
            @RequestBody ActualizarEstadoRequest request) {
        try {
            nodoService.actualizarEstado(idNodo, request.getEstado());
            return ResponseEntity.ok(new MessageResponse("Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // DTOs
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class RegistroNodoRequest {
        private String nombre;
        private String host;
        private Integer puertoRmi;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ActualizarEstadoRequest {
        private String estado;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String error;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }
}
