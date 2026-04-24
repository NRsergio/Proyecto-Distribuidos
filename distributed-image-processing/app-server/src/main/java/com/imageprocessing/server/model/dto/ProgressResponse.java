package com.imageprocessing.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de progreso de lote
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressResponse {
    private Long idLote;
    private String estado;
    private Integer progreso;
    private Integer cantidadImagenes;
    private Integer cantidadProcesadas;
    private String mensaje;
}
