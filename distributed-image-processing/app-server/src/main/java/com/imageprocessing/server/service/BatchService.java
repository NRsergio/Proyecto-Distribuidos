package com.imageprocessing.server.service;

import com.imageprocessing.server.model.dto.BatchRequest;
import com.imageprocessing.server.model.dto.BatchResponse;
import com.imageprocessing.server.model.dto.ProgressResponse;
import com.imageprocessing.server.model.dto.TransformacionDTO;
import com.imageprocessing.server.model.entity.ImagenSolicitud;
import com.imageprocessing.server.model.entity.SolicitudLote;
import com.imageprocessing.server.model.entity.Transformacion;
import com.imageprocessing.server.repository.ImagenSolicitudRepository;
import com.imageprocessing.server.repository.SolicitudLoteRepository;
import com.imageprocessing.server.repository.TransformacionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Procesamiento de Lotes
 * Maneja la creación, tracking y progreso de lotes de procesamiento
 */
@Service
@RequiredArgsConstructor
public class BatchService {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchService.class);
    
    private final SolicitudLoteRepository solicitudLoteRepository;
    private final TransformacionRepository transformacionRepository;
    private final ImagenSolicitudRepository imagenSolicitudRepository;
    
    /**
     * Crea una nueva solicitud de lote
     * @param idUsuario ID del usuario propietario
     * @param request datos del lote
     * @return BatchResponse con datos del lote creado
     */
    public BatchResponse crearLote(Long idUsuario, BatchRequest request) {
        logger.info("Creando nuevo lote para usuario: {}", idUsuario);
        
        // Crear solicitud de lote
        SolicitudLote lote = SolicitudLote.builder()
            .idUsuario(idUsuario)
            .titulo(request.getTitulo())
            .descripcion(request.getDescripcion())
            .estado("PENDIENTE")
            .fechaCreacion(LocalDateTime.now())
            .progreso(0)
            .cantidadImagenes(0)
            .cantidadProcesadas(0)
            .build();
        
        lote = solicitudLoteRepository.save(lote);
        
        // Crear transformaciones asociadas
        if (request.getTransformaciones() != null) {
            for (int i = 0; i < request.getTransformaciones().size(); i++) {
                TransformacionDTO transformDTO = request.getTransformaciones().get(i);
                Transformacion transformacion = Transformacion.builder()
                    .idLote(lote.getIdLote())
                    .tipo(transformDTO.getTipo())
                    .parametros(transformDTO.getParametros())
                    .orden(i)
                    .build();
                transformacionRepository.save(transformacion);
            }
        }
        
        logger.info("Lote creado exitosamente: {}", lote.getIdLote());
        
        return BatchResponse.builder()
            .idLote(lote.getIdLote())
            .titulo(lote.getTitulo())
            .estado(lote.getEstado())
            .cantidadImagenes(lote.getCantidadImagenes())
            .progreso(lote.getProgreso())
            .build();
    }
    
    /**
     * Agrega una imagen a un lote
     * @param idLote ID del lote
     * @param imagen imagen a agregar
     */
    public void agregarImagen(Long idLote, ImagenSolicitud imagen) {
        Optional<SolicitudLote> loteOpt = solicitudLoteRepository.findById(idLote);
        if (loteOpt.isEmpty()) {
            throw new RuntimeException("Lote no encontrado");
        }
        
        SolicitudLote lote = loteOpt.get();
        imagen.setIdLote(idLote);
        imagenSolicitudRepository.save(imagen);
        
        // Actualizar cantidad de imágenes
        lote.setCantidadImagenes(lote.getCantidadImagenes() + 1);
        solicitudLoteRepository.save(lote);
        
        logger.debug("Imagen agregada al lote: {}", idLote);
    }
    
    /**
     * Obtiene el progreso de un lote
     * @param idLote ID del lote
     * @return ProgressResponse con estado actual
     */
    public ProgressResponse obtenerProgreso(Long idLote) {
        Optional<SolicitudLote> loteOpt = solicitudLoteRepository.findById(idLote);
        if (loteOpt.isEmpty()) {
            throw new RuntimeException("Lote no encontrado");
        }
        
        SolicitudLote lote = loteOpt.get();
        
        // Recalcular progreso
        List<ImagenSolicitud> imagenes = imagenSolicitudRepository.findByIdLote(idLote);
        long completadas = imagenes.stream()
            .filter(img -> "COMPLETADA".equals(img.getEstado()))
            .count();
        
        int progreso = imagenes.isEmpty() ? 0 : (int) ((completadas * 100) / imagenes.size());
        lote.setProgreso(progreso);
        lote.setCantidadProcesadas((int) completadas);
        solicitudLoteRepository.save(lote);
        
        return ProgressResponse.builder()
            .idLote(lote.getIdLote())
            .estado(lote.getEstado())
            .progreso(lote.getProgreso())
            .cantidadImagenes(imagenes.size())
            .cantidadProcesadas(lote.getCantidadProcesadas())
            .build();
    }
    
    /**
     * Inicia el procesamiento de un lote
     * @param idLote ID del lote
     */
    public void iniciarProcesamiento(Long idLote) {
        Optional<SolicitudLote> loteOpt = solicitudLoteRepository.findById(idLote);
        if (loteOpt.isEmpty()) {
            throw new RuntimeException("Lote no encontrado");
        }
        
        SolicitudLote lote = loteOpt.get();
        lote.setEstado("PROCESANDO");
        lote.setFechaInicio(LocalDateTime.now());
        solicitudLoteRepository.save(lote);
        
        logger.info("Procesamiento iniciado para lote: {}", idLote);
    }
    
    /**
     * Completa el procesamiento de un lote
     * @param idLote ID del lote
     */
    public void completarProcesamiento(Long idLote) {
        Optional<SolicitudLote> loteOpt = solicitudLoteRepository.findById(idLote);
        if (loteOpt.isEmpty()) {
            throw new RuntimeException("Lote no encontrado");
        }
        
        SolicitudLote lote = loteOpt.get();
        lote.setEstado("COMPLETADO");
        lote.setFechaFin(LocalDateTime.now());
        lote.setProgreso(100);
        solicitudLoteRepository.save(lote);
        
        logger.info("Procesamiento completado para lote: {}", idLote);
    }
    
    /**
     * Marca un lote como error
     * @param idLote ID del lote
     * @param mensaje mensaje de error
     */
    public void marcarError(Long idLote, String mensaje) {
        Optional<SolicitudLote> loteOpt = solicitudLoteRepository.findById(idLote);
        if (loteOpt.isEmpty()) {
            throw new RuntimeException("Lote no encontrado");
        }
        
        SolicitudLote lote = loteOpt.get();
        lote.setEstado("ERROR");
        lote.setFechaFin(LocalDateTime.now());
        solicitudLoteRepository.save(lote);
        
        logger.error("Error en procesamiento de lote {}: {}", idLote, mensaje);
    }
}
