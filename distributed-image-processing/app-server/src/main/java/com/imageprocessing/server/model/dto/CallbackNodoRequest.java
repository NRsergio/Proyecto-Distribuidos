package com.imageprocessing.server.model.dto;

import lombok.Data;

@Data
public class CallbackNodoRequest {
    private Long idImagen;
    private Long idNodo;
    private boolean exitoso;
    private String rutaResultado;
    private String mensajeError;
    private long tiempoEjecucionMs;
}
