package com.aquacomunidad.backend.features.historial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.historial.entity.HistorialEstadoReporteEntidad;

public interface HistorialEstadoReporteRepositorio extends JpaRepository<HistorialEstadoReporteEntidad, Long> {

  List<HistorialEstadoReporteEntidad> findByReporteIdOrderByFechaCambioAsc(Long reporteId);
}
