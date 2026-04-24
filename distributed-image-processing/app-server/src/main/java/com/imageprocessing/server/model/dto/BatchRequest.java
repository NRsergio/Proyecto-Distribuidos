package com.imageprocessing.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para petición de envío de lote
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequest {
    private String titulo;
    private String descripcion;
    private List<TransformacionDTO> transformaciones;
}
