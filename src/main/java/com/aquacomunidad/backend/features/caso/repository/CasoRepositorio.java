package com.aquacomunidad.backend.features.caso.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;

public interface CasoRepositorio extends JpaRepository<CasoEntidad, Long> {
  @Override
  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  List<CasoEntidad> findAll();

  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  @Query("""
      select c from CasoEntidad c
      order by c.reporteOrigen.fechaCreacion desc, c.fechaAsignacion desc
      """)
  List<CasoEntidad> findAllOrdenadoPorReporteReciente();

  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  @Query("""
      select c from CasoEntidad c
      where c.responsable.id = :responsableId
      order by c.reporteOrigen.fechaCreacion desc, c.fechaAsignacion desc
      """)
  List<CasoEntidad> findByResponsableIdOrdenadoPorReporteReciente(@Param("responsableId") Long responsableId);

  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  @Query("""
      select c from CasoEntidad c
      where c.responsable.id = :responsableId and c.estado = :estado
      order by c.reporteOrigen.fechaCreacion desc, c.fechaAsignacion desc
      """)
  List<CasoEntidad> findByResponsableIdAndEstadoOrdenadoPorReporteReciente(
      @Param("responsableId") Long responsableId,
      @Param("estado") EstadoCaso estado);

  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  @Query("""
      select c from CasoEntidad c
      where c.estado = :estado
      order by c.reporteOrigen.fechaCreacion desc, c.fechaAsignacion desc
      """)
  List<CasoEntidad> findByEstadoOrdenadoPorReporteReciente(@Param("estado") EstadoCaso estado);

  long countByEstado(EstadoCaso estado);

  Optional<CasoEntidad> findByReporteOrigenId(Long reporteId);

  boolean existsByReporteOrigenId(Long reporteId);
}
