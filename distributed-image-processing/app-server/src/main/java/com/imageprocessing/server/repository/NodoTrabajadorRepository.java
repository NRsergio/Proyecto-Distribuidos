package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.NodoTrabajador;
import com.imageprocessing.server.model.enums.EstadoNodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface NodoTrabajadorRepository extends JpaRepository<NodoTrabajador, Long> {
    List<NodoTrabajador> findByEstado(EstadoNodo estado);
    Optional<NodoTrabajador> findByDireccionRed(String direccionRed);

    @Query("SELECT n FROM NodoTrabajador n WHERE n.estado = 'ACTIVO' ORDER BY n.cargaActual ASC")
    List<NodoTrabajador> findActivosOrdenadosPorCarga();
}
