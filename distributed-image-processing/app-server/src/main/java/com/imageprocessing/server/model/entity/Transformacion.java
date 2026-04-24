package com.imageprocessing.server.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Transformacion - Schema: public
 * Representa una transformación a aplicar a las imágenes
 */
@Entity
@Table(name = "transformacion", schema = "public", indexes = {
    @Index(name = "idx_transformacion_id_lote", columnList = "id_lote")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transformacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transformacion")
    private Long idTransformacion;
    
    @Column(name = "id_lote", nullable = false)
    private Long idLote;
    
    @Column(name = "tipo", nullable = false)
    private String tipo; // RESIZE, GRAYSCALE, BLUR, ROTATE, etc.
    
    @Column(name = "parametros")
    private String parametros; // JSON con parámetros específicos
    
    @Column(name = "orden", nullable = false)
    private Integer orden; // Orden de aplicación
}
