package com.aquacomunidad.backend.features.servicio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.servicio.entity.ZonaServicioEntidad;

public interface ZonaServicioRepositorio extends JpaRepository<ZonaServicioEntidad, Long> {
  Optional<ZonaServicioEntidad> findByNombreIgnoreCase(String nombre);
}
