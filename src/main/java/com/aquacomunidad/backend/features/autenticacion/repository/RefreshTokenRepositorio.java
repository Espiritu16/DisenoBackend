package com.aquacomunidad.backend.features.autenticacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.autenticacion.entity.RefreshTokenEntidad;

public interface RefreshTokenRepositorio extends JpaRepository<RefreshTokenEntidad, Long> {

  Optional<RefreshTokenEntidad> findByTokenId(String tokenId);

  List<RefreshTokenEntidad> findBySessionId(String sessionId);
}
