package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.NodoTrabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository para operaciones CRUD de NodoTrabajador
 */
@Repository
public interface NodoTrabajadorRepository extends JpaRepository<NodoTrabajador, Long> {
    Optional<NodoTrabajador> findByNombre(String nombre);
    List<NodoTrabajador> findByEstado(String estado);
    Optional<NodoTrabajador> findByHostAndPuertoRmi(String host, Integer puertoRmi);
}
