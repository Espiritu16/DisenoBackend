package com.aquacomunidad.backend.features.reporte.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;

public interface ReporteRepositorio extends JpaRepository<ReporteEntidad, Long> {
  List<ReporteEntidad> findByUsuarioId(Long usuarioId);

  List<ReporteEntidad> findByEstado(EstadoReporte estado);

  @Query("""
      select case when count(r) > 0 then true else false end
      from ReporteEntidad r
      where r.tipo = :tipo
        and r.zona = :zona
        and r.fechaCreacion >= :fechaDesde
      """)
  boolean existePosibleDuplicado(
      @Param("tipo") String tipo,
      @Param("zona") String zona,
      @Param("fechaDesde") LocalDateTime fechaDesde);
}
