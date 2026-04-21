package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.LogTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogTrabajoRepository extends JpaRepository<LogTrabajo, Long> {
    List<LogTrabajo> findByImagen_IdImagenOrderByFechaHoraAsc(Long idImagen);
}
