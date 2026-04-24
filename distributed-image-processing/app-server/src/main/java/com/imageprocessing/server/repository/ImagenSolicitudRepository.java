package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.ImagenSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para operaciones CRUD de ImagenSolicitud
 */
@Repository
public interface ImagenSolicitudRepository extends JpaRepository<ImagenSolicitud, Long> {
    List<ImagenSolicitud> findByIdLote(Long idLote);
    List<ImagenSolicitud> findByIdLoteAndEstado(Long idLote, String estado);
}
