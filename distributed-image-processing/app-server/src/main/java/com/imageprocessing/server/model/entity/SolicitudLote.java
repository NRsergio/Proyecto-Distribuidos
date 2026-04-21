package com.imageprocessing.server.model.entity;

import com.imageprocessing.server.model.enums.EstadoLote;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "solicitud_lote")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Long idLote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoLote estado;

    @Column(name = "porcentaje_progreso", precision = 5, scale = 2)
    private BigDecimal porcentajeProgreso;

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ImagenSolicitud> imagenes;
}
