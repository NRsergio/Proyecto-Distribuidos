package com.imageprocessing.server.repository;

import com.imageprocessing.server.model.entity.ImagenSolicitud;
import com.imageprocessing.server.model.enums.EstadoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ImagenSolicitudRepository extends JpaRepository<ImagenSolicitud, Long> {
    List<ImagenSolicitud> findByLote_IdLote(Long idLote);
    List<ImagenSolicitud> findByEstado(EstadoImagen estado);

    @Query("SELECT COUNT(i) FROM ImagenSolicitud i WHERE i.lote.idLote = :idLote AND i.estado = :estado")
    long countByLoteAndEstado(Long idLote, EstadoImagen estado);
}
