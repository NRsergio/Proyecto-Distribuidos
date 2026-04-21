package com.imageprocessing.server.controller;

import com.imageprocessing.server.model.dto.CallbackNodoRequest;
import com.imageprocessing.server.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/callback")
@RequiredArgsConstructor
public class CallbackController {

    private final BatchService batchService;

    @PostMapping("/imagen")
    public ResponseEntity<Void> recibirCallbackImagen(@RequestBody CallbackNodoRequest request) {
        log.info("Callback recibido: imagen={}, exitoso={}", request.getIdImagen(), request.isExitoso());
        batchService.procesarCallbackNodo(request);
        return ResponseEntity.ok().build();
    }
}
