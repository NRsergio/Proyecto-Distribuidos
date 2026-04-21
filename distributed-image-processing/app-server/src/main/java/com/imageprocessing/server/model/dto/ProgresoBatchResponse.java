package com.imageprocessing.server.model.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class ProgresoBatchResponse {
    private Long idLote;
    private String estadoLote;
    private BigDecimal porcentajeProgreso;
    private int totalImagenes;
    private int imagenesCompletadas;
    private int imagenesError;
    private List<ImagenProgresoDto> imagenes;

    @Data @Builder
    public static class ImagenProgresoDto {
        private Long idImagen;
        private String nombreArchivo;
        private String estado;
        private String rutaResultado;
    }
}
