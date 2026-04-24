package com.imageprocessing.server.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad ImagenSolicitud - Schema: public
 * Representa una imagen dentro de una solicitud de lote
 */
@Entity
@Table(name = "imagen_solicitud", schema = "public", indexes = {
    @Index(name = "idx_imagen_id_lote", columnList = "id_lote"),
    @Index(name = "idx_imagen_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenSolicitud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long idImagen;
    
    @Column(name = "id_lote", nullable = false)
    private Long idLote;
    
    @Column(name = "ruta_original", nullable = false)
    private String rutaOriginal;
    
    @Column(name = "ruta_procesada")
    private String rutaProcesada;
    
    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;
    
    @Column(name = "tipo_mime", nullable = false)
    private String tipoMime;
    
    @Column(name = "tamaño_bytes", nullable = false)
    private Long tamañoBytes;
    
    @Column(name = "estado", nullable = false)
    private String estado; // PENDIENTE, PROCESANDO, COMPLETADA, ERROR
    
    @Column(name = "mensaje_error")
    private String mensajeError;
}
