package com.aquacomunidad.backend.features.autenticacion.support;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class GeneradorCodigoRecuperacion {

  private static final SecureRandom RANDOM = new SecureRandom();

  public String generarCodigoSeisDigitos() {
    int valor = RANDOM.nextInt(900000) + 100000;
    return Integer.toString(valor);
  }
}
