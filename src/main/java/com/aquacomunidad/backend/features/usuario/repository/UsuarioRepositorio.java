package com.aquacomunidad.backend.features.usuario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.usuario.entity.UsuarioEntidad;

public interface UsuarioRepositorio extends JpaRepository<UsuarioEntidad, Long> {

  Optional<UsuarioEntidad> findByCorreoIgnoreCase(String correo);

  boolean existsByCorreoIgnoreCase(String correo);
}
