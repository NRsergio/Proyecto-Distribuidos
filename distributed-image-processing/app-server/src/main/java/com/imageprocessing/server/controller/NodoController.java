package com.imageprocessing.server.controller;

import com.imageprocessing.server.model.entity.NodoTrabajador;
import com.imageprocessing.server.service.NodoManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/nodos")
@RequiredArgsConstructor
public class NodoController {

    private final NodoManagerService nodoManagerService;

    @PostMapping("/registrar")
    public ResponseEntity<NodoTrabajador> registrarNodo(@RequestBody Map<String, String> body) {
        NodoTrabajador nodo = nodoManagerService.registrarNodo(
            body.get("nombre"),
            body.get("direccionRed")
        );
        return ResponseEntity.ok(nodo);
    }
}
