package com.aquacomunidad.backend.features.autenticacion.entity;

public enum EstadoTokenRecuperacion {
  PENDIENTE,
  CODIGO_CONFIRMADO,
  USADO,
  EXPIRADO,
  CANCELADO
}
