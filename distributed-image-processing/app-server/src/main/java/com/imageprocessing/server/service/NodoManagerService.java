package com.imageprocessing.server.service;

import com.imageprocessing.server.grpc.NodeGrpcClient;
import com.imageprocessing.server.model.entity.*;
import com.imageprocessing.server.model.enums.*;
import com.imageprocessing.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodoManagerService {

    private final NodoTrabajadorRepository nodoRepository;
    private final ImagenSolicitudRepository imagenRepository;
    private final NodeGrpcClient grpcClient;

    @Value("${app.callback-base-url}")
    private String callbackBaseUrl;

    /**
     * Distribuye las imagenes pendientes de un lote entre los nodos activos.
     * Se ejecuta de forma asincrona para no bloquear la respuesta SOAP.
     */
    @Async
    @Transactional
    public void distribuirImagenes(Long idLote) {
        List<ImagenSolicitud> imagenes = imagenRepository.findByLote_IdLote(idLote)
            .stream()
            .filter(i -> i.getEstado() == EstadoImagen.PENDIENTE)
            .toList();

        List<NodoTrabajador> nodosActivos = nodoRepository.findActivosOrdenadosPorCarga();

        if (nodosActivos.isEmpty()) {
            log.error("No hay nodos activos para procesar el lote {}", idLote);
            return;
        }

        int indiceNodo = 0;
        for (ImagenSolicitud imagen : imagenes) {
            NodoTrabajador nodo = nodosActivos.get(indiceNodo % nodosActivos.size());

            imagen.setNodo(nodo);
            imagen.setEstado(EstadoImagen.EN_PROCESO);
            imagenRepository.save(imagen);

            nodo.setCargaActual(nodo.getCargaActual() == null ? 1 : nodo.getCargaActual() + 1);
            nodoRepository.save(nodo);

            // Enviar trabajo al nodo via gRPC
            grpcClient.enviarTrabajoANodo(nodo, imagen, callbackBaseUrl);

            indiceNodo++;
            log.info("Imagen {} asignada al nodo {}", imagen.getIdImagen(), nodo.getNombre());
        }
    }

    /**
     * Registra o actualiza un nodo en el sistema.
     */
    @Transactional
    public NodoTrabajador registrarNodo(String nombre, String direccionRed) {
        return nodoRepository.findByDireccionRed(direccionRed)
            .map(n -> {
                n.setEstado(EstadoNodo.ACTIVO);
                n.setUltimaConexion(LocalDateTime.now());
                return nodoRepository.save(n);
            })
            .orElseGet(() -> nodoRepository.save(NodoTrabajador.builder()
                .nombre(nombre)
                .direccionRed(direccionRed)
                .estado(EstadoNodo.ACTIVO)
                .cargaActual(0)
                .ultimaConexion(LocalDateTime.now())
                .build()));
    }

    /**
     * Heartbeat scheduler: marca como INACTIVO los nodos sin respuesta reciente.
     */
    @Transactional
    public void verificarNodosActivos() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(2);
        List<NodoTrabajador> nodosActivos = nodoRepository.findByEstado(EstadoNodo.ACTIVO);

        for (NodoTrabajador nodo : nodosActivos) {
            if (nodo.getUltimaConexion() != null && nodo.getUltimaConexion().isBefore(limite)) {
                nodo.setEstado(EstadoNodo.INACTIVO);
                nodoRepository.save(nodo);
                log.warn("Nodo {} marcado INACTIVO (sin heartbeat)", nodo.getNombre());
            }
        }
    }
}
