package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.Transformacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para operaciones CRUD de Transformacion
 */
@Repository
public interface TransformacionRepository extends JpaRepository<Transformacion, Long> {
    List<Transformacion> findByIdLote(Long idLote);
}
