package com.imageprocessing.server.model.entity;

import com.imageprocessing.server.model.enums.NivelLog;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_trabajo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_imagen", nullable = false)
    private ImagenSolicitud imagen;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, length = 20)
    private NivelLog nivel;

    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
}
