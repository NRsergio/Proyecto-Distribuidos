package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.SolicitudLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para operaciones CRUD de SolicitudLote
 */
@Repository
public interface SolicitudLoteRepository extends JpaRepository<SolicitudLote, Long> {
    List<SolicitudLote> findByIdUsuario(Long idUsuario);
    List<SolicitudLote> findByEstado(String estado);
    List<SolicitudLote> findByIdUsuarioAndEstado(Long idUsuario, String estado);
}
