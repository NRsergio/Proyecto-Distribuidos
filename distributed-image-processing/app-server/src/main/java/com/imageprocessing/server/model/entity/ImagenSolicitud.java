package com.imageprocessing.server.model.entity;

import com.imageprocessing.server.model.enums.EstadoImagen;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "imagen_solicitud")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImagenSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long idImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", nullable = false)
    private SolicitudLote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nodo")
    private NodoTrabajador nodo;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_original", nullable = false, length = 255)
    private String rutaOriginal;

    @Column(name = "ruta_resultado", length = 255)
    private String rutaResultado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoImagen estado;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @OneToMany(mappedBy = "imagen", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Transformacion> transformaciones;

    @OneToMany(mappedBy = "imagen", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LogTrabajo> logs;
}
