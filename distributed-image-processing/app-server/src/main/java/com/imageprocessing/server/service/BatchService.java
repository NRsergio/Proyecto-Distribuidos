package com.imageprocessing.server.service;

import com.imageprocessing.server.model.dto.*;
import com.imageprocessing.server.model.entity.*;
import com.imageprocessing.server.model.enums.*;
import com.imageprocessing.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final SolicitudLoteRepository loteRepository;
    private final ImagenSolicitudRepository imagenRepository;
    private final UsuarioRepository usuarioRepository;
    private final NodoManagerService nodoManagerService;
    private final LogTrabajoRepository logRepository;

    @Transactional
    public EnviarLoteResponse procesarEnvioLote(Long idUsuario, EnviarLoteRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));

        // Registrar el lote
        SolicitudLote lote = SolicitudLote.builder()
            .usuario(usuario)
            .fechaRecepcion(LocalDateTime.now())
            .estado(EstadoLote.PENDIENTE)
            .porcentajeProgreso(BigDecimal.ZERO)
            .build();
        lote = loteRepository.save(lote);
        log.info("Lote {} creado para usuario {}", lote.getIdLote(), idUsuario);

        // Registrar cada imagen
        for (EnviarLoteRequest.ImagenRequest imgReq : request.getImagenes()) {
            ImagenSolicitud imagen = ImagenSolicitud.builder()
                .lote(lote)
                .nombreArchivo(imgReq.getNombreArchivo())
                .rutaOriginal(imgReq.getRutaOriginal())
                .estado(EstadoImagen.PENDIENTE)
                .fechaRecepcion(LocalDateTime.now())
                .build();

            List<Transformacion> transformaciones = imgReq.getTransformaciones().stream()
                .map(t -> Transformacion.builder()
                    .imagen(imagen)
                    .tipo(t.getTipo())
                    .orden(t.getOrden())
                    .parametros(t.getParametros())
                    .build())
                .collect(Collectors.toList());

            imagen.setTransformaciones(transformaciones);
            imagenRepository.save(imagen);
        }

        // Disparar distribucion en hilo separado
        nodoManagerService.distribuirImagenes(lote.getIdLote());

        return EnviarLoteResponse.builder()
            .idLote(lote.getIdLote())
            .aceptado(true)
            .mensaje("Lote registrado y en proceso de distribucion")
            .build();
    }

    @Transactional
    public void procesarCallbackNodo(CallbackNodoRequest callback) {
        ImagenSolicitud imagen = imagenRepository.findById(callback.getIdImagen())
            .orElseThrow(() -> new RuntimeException("Imagen no encontrada: " + callback.getIdImagen()));

        if (callback.isExitoso()) {
            imagen.setEstado(EstadoImagen.COMPLETADO);
            imagen.setRutaResultado(callback.getRutaResultado());
            registrarLog(imagen, NivelLog.INFO,
                "Procesamiento exitoso en " + callback.getTiempoEjecucionMs() + "ms");
        } else {
            imagen.setEstado(EstadoImagen.ERROR);
            registrarLog(imagen, NivelLog.ERROR,
                "Error en nodo: " + callback.getMensajeError());
        }

        imagen.setFechaProcesamiento(LocalDateTime.now());
        imagenRepository.save(imagen);

        // Actualizar progreso del lote
        actualizarProgresoBatch(imagen.getLote().getIdLote());
    }

    public ProgresoBatchResponse consultarProgreso(Long idLote) {
        SolicitudLote lote = loteRepository.findById(idLote)
            .orElseThrow(() -> new RuntimeException("Lote no encontrado: " + idLote));

        List<ImagenSolicitud> imagenes = imagenRepository.findByLote_IdLote(idLote);
        long completadas = imagenes.stream().filter(i -> i.getEstado() == EstadoImagen.COMPLETADO).count();
        long errores = imagenes.stream().filter(i -> i.getEstado() == EstadoImagen.ERROR).count();

        List<ProgresoBatchResponse.ImagenProgresoDto> imagenesDto = imagenes.stream()
            .map(i -> ProgresoBatchResponse.ImagenProgresoDto.builder()
                .idImagen(i.getIdImagen())
                .nombreArchivo(i.getNombreArchivo())
                .estado(i.getEstado().name())
                .rutaResultado(i.getRutaResultado())
                .build())
            .collect(Collectors.toList());

        return ProgresoBatchResponse.builder()
            .idLote(idLote)
            .estadoLote(lote.getEstado().name())
            .porcentajeProgreso(lote.getPorcentajeProgreso())
            .totalImagenes(imagenes.size())
            .imagenesCompletadas((int) completadas)
            .imagenesError((int) errores)
            .imagenes(imagenesDto)
            .build();
    }

    private void actualizarProgresoBatch(Long idLote) {
        SolicitudLote lote = loteRepository.findById(idLote).orElseThrow();
        List<ImagenSolicitud> imagenes = imagenRepository.findByLote_IdLote(idLote);

        long total = imagenes.size();
        long terminadas = imagenes.stream()
            .filter(i -> i.getEstado() == EstadoImagen.COMPLETADO || i.getEstado() == EstadoImagen.ERROR)
            .count();

        BigDecimal progreso = total == 0 ? BigDecimal.ZERO
            : BigDecimal.valueOf(terminadas * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        lote.setPorcentajeProgreso(progreso);

        if (terminadas == total) {
            boolean hayErrores = imagenes.stream().anyMatch(i -> i.getEstado() == EstadoImagen.ERROR);
            lote.setEstado(hayErrores ? EstadoLote.COMPLETADO_CON_ERRORES : EstadoLote.COMPLETADO);
        } else {
            lote.setEstado(EstadoLote.EN_PROCESO);
        }
        loteRepository.save(lote);
    }

    private void registrarLog(ImagenSolicitud imagen, NivelLog nivel, String mensaje) {
        logRepository.save(LogTrabajo.builder()
            .imagen(imagen)
            .nivel(nivel)
            .mensaje(mensaje)
            .fechaHora(LocalDateTime.now())
            .build());
    }
}
