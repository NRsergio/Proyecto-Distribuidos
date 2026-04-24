package com.imageprocessing.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para una transformación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransformacionDTO {
    private String tipo; // RESIZE, GRAYSCALE, BLUR, ROTATE, etc.
    private String parametros; // JSON string con parámetros
}
