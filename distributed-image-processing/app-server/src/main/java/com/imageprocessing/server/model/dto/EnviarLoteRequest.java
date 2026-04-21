package com.imageprocessing.server.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnviarLoteRequest {
    private String token;
    private List<ImagenRequest> imagenes;

    @Data
    public static class ImagenRequest {
        private String nombreArchivo;
        private String rutaOriginal;
        private List<TransformacionRequest> transformaciones;
    }

    @Data
    public static class TransformacionRequest {
        private String tipo;
        private Integer orden;
        private String parametros;
    }
}
