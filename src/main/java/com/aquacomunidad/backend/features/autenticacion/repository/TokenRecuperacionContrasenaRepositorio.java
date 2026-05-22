package com.aquacomunidad.backend.features.autenticacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquacomunidad.backend.features.autenticacion.entity.EstadoTokenRecuperacion;
import com.aquacomunidad.backend.features.autenticacion.entity.TokenRecuperacionContrasenaEntidad;

public interface TokenRecuperacionContrasenaRepositorio extends JpaRepository<TokenRecuperacionContrasenaEntidad, Long> {

  Optional<TokenRecuperacionContrasenaEntidad> findTopByUsuarioIdAndEstadoOrderByCreadoEnDesc(
      Long usuarioId,
      EstadoTokenRecuperacion estado);

  List<TokenRecuperacionContrasenaEntidad> findByUsuarioIdAndEstadoIn(
      Long usuarioId,
      List<EstadoTokenRecuperacion> estados);
}
