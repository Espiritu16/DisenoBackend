package com.aquacomunidad.backend.features.reporte.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.reporte.entity.ReporteImagenEntidad;

public interface ReporteImagenRepositorio extends JpaRepository<ReporteImagenEntidad, Long> {
}
