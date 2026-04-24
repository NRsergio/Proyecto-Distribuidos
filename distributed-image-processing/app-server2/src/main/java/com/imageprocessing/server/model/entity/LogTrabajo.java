package com.imageprocessing.server.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad LogTrabajo - Schema: public
 * Representa el log o historial de un trabajo procesado
 */
@Entity
@Table(name = "log_trabajo", schema = "public", indexes = {
    @Index(name = "idx_log_id_lote", columnList = "id_lote"),
    @Index(name = "idx_log_id_nodo", columnList = "id_nodo"),
    @Index(name = "idx_log_fecha", columnList = "fecha_inicio")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogTrabajo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;
    
    @Column(name = "id_lote", nullable = false)
    private Long idLote;
    
    @Column(name = "id_imagen", nullable = false)
    private Long idImagen;
    
    @Column(name = "id_nodo", nullable = false)
    private Long idNodo;
    
    @Column(name = "estado", nullable = false)
    private String estado; // INICIADO, COMPLETADO, ERROR
    
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
    
    @Column(name = "tiempo_procesamiento")
    private Long tiempoMs; // Tiempo en milisegundos
    
    @Column(name = "mensaje")
    private String mensaje;
    
    @Column(name = "detalles_error")
    private String detallesError;
}
