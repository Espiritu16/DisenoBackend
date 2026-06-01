package com.aquacomunidad.backend.features.caso.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.common.enums.EstadoCaso;
import com.aquacomunidad.backend.features.caso.entity.CasoEntidad;

public interface CasoRepositorio extends JpaRepository<CasoEntidad, Long> {
  @Override
  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  List<CasoEntidad> findAll();

  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  List<CasoEntidad> findByResponsableId(Long responsableId);

  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  List<CasoEntidad> findByResponsableIdAndEstado(Long responsableId, EstadoCaso estado);

  @EntityGraph(attributePaths = {"reporteOrigen", "responsable", "evidencias"})
  List<CasoEntidad> findByEstado(EstadoCaso estado);

  long countByEstado(EstadoCaso estado);

  Optional<CasoEntidad> findByReporteOrigenId(Long reporteId);

  boolean existsByReporteOrigenId(Long reporteId);
}
