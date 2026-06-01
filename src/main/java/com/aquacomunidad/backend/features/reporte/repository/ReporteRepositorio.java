package com.aquacomunidad.backend.features.reporte.repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aquacomunidad.backend.common.enums.EstadoReporte;
import com.aquacomunidad.backend.features.reporte.entity.ReporteEntidad;

public interface ReporteRepositorio extends JpaRepository<ReporteEntidad, Long> {
  @Override
  @EntityGraph(attributePaths = {"usuario", "tipo", "imagenes"})
  List<ReporteEntidad> findAll();

  @EntityGraph(attributePaths = {"usuario", "tipo", "imagenes"})
  List<ReporteEntidad> findByUsuarioId(Long usuarioId);

  @EntityGraph(attributePaths = {"usuario", "tipo", "imagenes"})
  List<ReporteEntidad> findByEstado(EstadoReporte estado);

  long countByEstado(EstadoReporte estado);

  @EntityGraph(attributePaths = {"tipo"})
  Optional<ReporteEntidad> findTopByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

  @EntityGraph(attributePaths = {"usuario", "tipo", "imagenes"})
  Optional<ReporteEntidad> findByIdAndUsuarioId(Long id, Long usuarioId);

  @EntityGraph(attributePaths = {"tipo"})
  List<ReporteEntidad> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

  List<ReporteEntidad> findByFechaCreacionBetweenOrderByFechaCreacionAsc(
      LocalDateTime fechaDesde,
      LocalDateTime fechaHasta);

  @Query("""
      select distinct r
      from ReporteEntidad r
      join fetch r.usuario
      join fetch r.tipo
      left join fetch r.imagenes
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
      select r.estado as estado, count(r) as total
      from ReporteEntidad r
      where r.usuario.id = :usuarioId
      group by r.estado
      """)
  List<ReporteConteoEstado> contarPorEstadoDeUsuario(@Param("usuarioId") Long usuarioId);

  @Query("""
      select r.estado as estado, count(r) as total
      from ReporteEntidad r
      group by r.estado
      """)
  List<ReporteConteoEstado> contarPorEstadoGlobal();

  @Query("""
      select function('date', r.fechaCreacion) as fecha, count(r) as total
      from ReporteEntidad r
      where r.fechaCreacion >= :fechaDesde and r.fechaCreacion < :fechaHasta
      group by function('date', r.fechaCreacion)
      order by function('date', r.fechaCreacion) asc
      """)
  List<ReporteConteoPorDia> contarPorDia(
      @Param("fechaDesde") LocalDateTime fechaDesde,
      @Param("fechaHasta") LocalDateTime fechaHasta);

  @Query("""
      select r.zona as zona, count(r) as total
      from ReporteEntidad r
      group by r.zona
      order by count(r) desc, r.zona asc
      """)
  List<ReporteConteoPorZona> contarZonas(Pageable pageable);

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
