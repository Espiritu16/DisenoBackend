package com.aquacomunidad.backend.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aquacomunidad.backend.common.response.RespuestaApi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ManejadorGlobalExcepcion {

  @ExceptionHandler(ExcepcionApi.class)
  public ResponseEntity<RespuestaApi<Object>> handleExcepcionApi(
      ExcepcionApi ex,
      HttpServletRequest request) {
    return ResponseEntity.status(ex.getStatus())
        .body(RespuestaApi.error(ex.getMessage(), null, request.getRequestURI()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<RespuestaApi<Map<String, String>>> handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    return ResponseEntity.badRequest()
        .body(RespuestaApi.error("Validation error", errors, request.getRequestURI()));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<RespuestaApi<Object>> handleConstraintViolation(
      ConstraintViolationException ex,
      HttpServletRequest request) {
    return ResponseEntity.badRequest()
        .body(RespuestaApi.error(ex.getMessage(), null, request.getRequestURI()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<RespuestaApi<Object>> handleGeneric(
      Exception ex,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(RespuestaApi.error("Internal server error", null, request.getRequestURI()));
  }
}
