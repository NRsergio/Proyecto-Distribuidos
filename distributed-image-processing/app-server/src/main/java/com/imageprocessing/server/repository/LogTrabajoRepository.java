package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.LogTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para operaciones CRUD de LogTrabajo
 */
@Repository
public interface LogTrabajoRepository extends JpaRepository<LogTrabajo, Long> {
    List<LogTrabajo> findByIdLote(Long idLote);
    List<LogTrabajo> findByIdNodo(Long idNodo);
    List<LogTrabajo> findByIdLoteAndIdImagen(Long idLote, Long idImagen);
    Integer countByIdNodo(Long idNodo);
}
