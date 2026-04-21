package com.imageprocessing.server.scheduler;

import com.imageprocessing.server.service.NodoManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatScheduler {

    private final NodoManagerService nodoManagerService;

    @Scheduled(fixedRateString = "${app.heartbeat-interval-ms}")
    public void verificarEstadoNodos() {
        log.debug("Ejecutando verificacion de nodos activos...");
        nodoManagerService.verificarNodosActivos();
    }
}
