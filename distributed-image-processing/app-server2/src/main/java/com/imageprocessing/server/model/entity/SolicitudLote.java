package com.imageprocessing.server.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad SolicitudLote - Schema: public
 * Representa una solicitud de procesamiento de lote de imágenes
 */
@Entity
@Table(name = "solicitud_lote", schema = "public", indexes = {
    @Index(name = "idx_solicitud_lote_usuario", columnList = "id_usuario"),
    @Index(name = "idx_solicitud_lote_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudLote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Long idLote;
    
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;
    
    @Column(name = "titulo", nullable = false)
    private String titulo;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "estado", nullable = false)
    private String estado; // PENDIENTE, PROCESANDO, COMPLETADO, ERROR
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
    
    @Column(name = "progreso", nullable = false)
    private Integer progreso = 0;
    
    @Column(name = "cantidad_imagenes", nullable = false)
    private Integer cantidadImagenes = 0;
    
    @Column(name = "cantidad_procesadas", nullable = false)
    private Integer cantidadProcesadas = 0;
}
