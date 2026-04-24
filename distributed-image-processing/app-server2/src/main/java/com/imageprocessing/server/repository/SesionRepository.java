package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para operaciones CRUD de Sesion
 */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {
    Optional<Sesion> findByToken(String token);
    List<Sesion> findByUsuarioIdUsuario(Long idUsuario);
}
