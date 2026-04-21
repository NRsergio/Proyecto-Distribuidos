package com.imageprocessing.server.model.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class EnviarLoteResponse {
    private Long idLote;
    private boolean aceptado;
    private String mensaje;
}
