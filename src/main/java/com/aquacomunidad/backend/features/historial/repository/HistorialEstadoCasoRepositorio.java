package com.aquacomunidad.backend.features.historial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.historial.entity.HistorialEstadoCasoEntidad;

public interface HistorialEstadoCasoRepositorio extends JpaRepository<HistorialEstadoCasoEntidad, Long> {

  List<HistorialEstadoCasoEntidad> findByCasoIdOrderByFechaCambioAsc(Long casoId);
}
