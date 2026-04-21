package com.imageprocessing.server.model.entity;

import com.imageprocessing.server.model.enums.EstadoNodo;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nodo_trabajador")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NodoTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nodo")
    private Long idNodo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "direccion_red", nullable = false, length = 155)
    private String direccionRed;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoNodo estado;

    @Column(name = "carga_actual")
    private Integer cargaActual;

    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;
}
