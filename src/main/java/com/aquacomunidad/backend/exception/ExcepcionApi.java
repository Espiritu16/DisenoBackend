package com.aquacomunidad.backend.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ExcepcionApi extends RuntimeException {

  private final HttpStatus status;

  public ExcepcionApi(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
