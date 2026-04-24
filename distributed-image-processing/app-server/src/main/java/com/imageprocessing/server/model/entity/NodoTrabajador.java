package com.imageprocessing.server.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad NodoTrabajador - Schema: public
 * Representa un nodo trabajador conectado al sistema
 */
@Entity
@Table(name = "nodo_trabajador", schema = "public", indexes = {
    @Index(name = "idx_nodo_estado", columnList = "estado"),
    @Index(name = "idx_nodo_ultima_conexion", columnList = "ultima_conexion")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodoTrabajador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nodo")
    private Long idNodo;
    
    @Column(name = "nombre", unique = true, nullable = false)
    private String nombre;
    
    @Column(name = "host", nullable = false)
    private String host;
    
    @Column(name = "puerto_rmi", nullable = false)
    private Integer puertoRmi;
    
    @Column(name = "estado", nullable = false)
    private String estado; // CONECTADO, DESCONECTADO, PROCESANDO
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;
    
    @Column(name = "cpu_utilizado")
    private Double cpuUtilizado;
    
    @Column(name = "memoria_utilizada")
    private Long memoriaUtilizada;
    
    @Column(name = "trabajos_completados", nullable = false)
    @Builder.Default
    private Integer trabajosCompletados = 0;
}
