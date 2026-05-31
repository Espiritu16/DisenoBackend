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

  long countByEstado(EstadoReporte estado);

  List<ReporteEntidad> findByFechaCreacionBetweenOrderByFechaCreacionAsc(
      LocalDateTime fechaDesde,
      LocalDateTime fechaHasta);

  @Query("""
      select r
      from ReporteEntidad r
      where (:usuarioId is null or r.usuario.id = :usuarioId)
        and (:estado is null or r.estado = :estado)
        and (:tipo is null or lower(r.tipo.nombre) like lower(concat('%', :tipo, '%'))
            or lower(r.tipo.codigo) like lower(concat('%', :tipo, '%')))
        and (:zona is null or lower(r.zona) like lower(concat('%', :zona, '%')))
        and (:fechaDesde is null or r.fechaCreacion >= :fechaDesde)
        and (:fechaHasta is null or r.fechaCreacion <= :fechaHasta)
      order by r.fechaCreacion desc
      """)
  List<ReporteEntidad> buscarConFiltros(
      @Param("usuarioId") Long usuarioId,
      @Param("estado") EstadoReporte estado,
      @Param("tipo") String tipo,
      @Param("zona") String zona,
      @Param("fechaDesde") LocalDateTime fechaDesde,
      @Param("fechaHasta") LocalDateTime fechaHasta);

  @Query("""
      select case when count(r) > 0 then true else false end
      from ReporteEntidad r
      where lower(r.tipo.nombre) = lower(:tipo)
        and r.zona = :zona
        and r.fechaCreacion >= :fechaDesde
      """)
  boolean existePosibleDuplicado(
      @Param("tipo") String tipo,
      @Param("zona") String zona,
      @Param("fechaDesde") LocalDateTime fechaDesde);
}
