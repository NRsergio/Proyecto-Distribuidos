package com.imageprocessing.server.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transformacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transformacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transformacion")
    private Long idTransformacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_imagen", nullable = false)
    private ImagenSolicitud imagen;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "parametros", columnDefinition = "TEXT")
    private String parametros;  // JSON con configuracion especifica
}
