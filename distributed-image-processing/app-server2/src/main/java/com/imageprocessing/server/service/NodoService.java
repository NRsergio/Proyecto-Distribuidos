package com.imageprocessing.server.service;

import com.imageprocessing.server.model.entity.NodoTrabajador;
import com.imageprocessing.server.repository.LogTrabajoRepository;
import com.imageprocessing.server.repository.NodoTrabajadorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Gestión de Nodos
 * Maneja registro, monitoreo y comunicación RMI con nodos trabajadores
 */
@Service
@RequiredArgsConstructor
public class NodoService {
    
    private static final Logger logger = LoggerFactory.getLogger(NodoService.class);
    
    private final NodoTrabajadorRepository nodoRepository;
    private final LogTrabajoRepository logTrabajoRepository;
    
    /**
     * Registra un nuevo nodo trabajador
     * @param nombre nombre único del nodo
     * @param host dirección IP del nodo
     * @param puertoRmi puerto RMI del nodo
     * @return NodoTrabajador registrado
     */
    public NodoTrabajador registrarNodo(String nombre, String host, Integer puertoRmi) {
        logger.info("Registrando nodo: {} en {}:{}", nombre, host, puertoRmi);
        
        if (nodoRepository.findByNombre(nombre).isPresent()) {
            throw new RuntimeException("El nodo ya existe");
        }
        
        NodoTrabajador nodo = NodoTrabajador.builder()
            .nombre(nombre)
            .host(host)
            .puertoRmi(puertoRmi)
            .estado("CONECTADO")
            .fechaRegistro(LocalDateTime.now())
            .ultimaConexion(LocalDateTime.now())
            .trabajosCompletados(0)
            .build();
        
        nodo = nodoRepository.save(nodo);
        logger.info("Nodo registrado exitosamente: {}", nombre);
        
        return nodo;
    }
    
    /**
     * Obtiene todos los nodos conectados
     * @return lista de nodos conectados
     */
    public List<NodoTrabajador> obtenerNodosActivos() {
        return nodoRepository.findByEstado("CONECTADO");
    }
    
    /**
     * Actualiza el estado de un nodo
     * @param idNodo ID del nodo
     * @param estado nuevo estado
     */
    public void actualizarEstado(Long idNodo, String estado) {
        Optional<NodoTrabajador> nodoOpt = nodoRepository.findById(idNodo);
        if (nodoOpt.isEmpty()) {
            throw new RuntimeException("Nodo no encontrado");
        }
        
        NodoTrabajador nodo = nodoOpt.get();
        nodo.setEstado(estado);
        nodo.setUltimaConexion(LocalDateTime.now());
        nodoRepository.save(nodo);
        
        logger.debug("Estado de nodo {} actualizado a: {}", idNodo, estado);
    }
    
    /**
     * Actualiza métricas de un nodo
     * @param idNodo ID del nodo
     * @param cpuUtilizado % de CPU utilizado
     * @param memoriaUtilizada MB de memoria utilizada
     */
    public void actualizarMetricas(Long idNodo, Double cpuUtilizado, Long memoriaUtilizada) {
        Optional<NodoTrabajador> nodoOpt = nodoRepository.findById(idNodo);
        if (nodoOpt.isEmpty()) {
            return;
        }
        
        NodoTrabajador nodo = nodoOpt.get();
        nodo.setCpuUtilizado(cpuUtilizado);
        nodo.setMemoriaUtilizada(memoriaUtilizada);
        nodoRepository.save(nodo);
    }
    
    /**
     * Marca un nodo como completado un trabajo
     * @param idNodo ID del nodo
     */
    public void incrementarTrabajosCompletados(Long idNodo) {
        Optional<NodoTrabajador> nodoOpt = nodoRepository.findById(idNodo);
        if (nodoOpt.isEmpty()) {
            return;
        }
        
        NodoTrabajador nodo = nodoOpt.get();
        nodo.setTrabajosCompletados(nodo.getTrabajosCompletados() + 1);
        nodoRepository.save(nodo);
    }
    
    /**
     * Desconecta un nodo
     * @param idNodo ID del nodo
     */
    public void desconectarNodo(Long idNodo) {
        Optional<NodoTrabajador> nodoOpt = nodoRepository.findById(idNodo);
        if (nodoOpt.isEmpty()) {
            return;
        }
        
        NodoTrabajador nodo = nodoOpt.get();
        nodo.setEstado("DESCONECTADO");
        nodoRepository.save(nodo);
        
        logger.info("Nodo desconectado: {}", idNodo);
    }
}
