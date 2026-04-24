package com.imageprocessing.server.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad Sesion - Schema: auth
 * Representa una sesión de usuario activa
 */
@Entity
@Table(name = "sesiones", schema = "auth", indexes = {
    @Index(name = "idx_sesiones_token", columnList = "token", unique = true),
    @Index(name = "idx_sesiones_id_usuario", columnList = "id_usuario")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sesion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Long idSesion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @Column(name = "token", unique = true, nullable = false)
    private String token;
    
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;
    
    @Column(name = "dispositivo_info")
    private String dispositivoInfo;
}
