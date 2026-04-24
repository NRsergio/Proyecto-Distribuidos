package com.imageprocessing.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de creación de lote
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchResponse {
    private Long idLote;
    private String titulo;
    private String estado;
    private Integer cantidadImagenes;
    private Integer progreso;
}
