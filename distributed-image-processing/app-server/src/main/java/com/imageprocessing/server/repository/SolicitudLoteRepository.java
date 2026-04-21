package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.SolicitudLote;
import com.imageprocessing.server.model.enums.EstadoLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SolicitudLoteRepository extends JpaRepository<SolicitudLote, Long> {
    List<SolicitudLote> findByUsuario_IdUsuarioOrderByFechaRecepcionDesc(Long idUsuario);
    List<SolicitudLote> findByEstado(EstadoLote estado);
}
