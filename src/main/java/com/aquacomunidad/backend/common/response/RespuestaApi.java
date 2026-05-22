package com.aquacomunidad.backend.common.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RespuestaApi<T> {

  private final LocalDateTime timestamp;
  private final boolean success;
  private final String message;
  private final T data;
  private final String path;

  public static <T> RespuestaApi<T> ok(String message, T data, String path) {
    return RespuestaApi.<T>builder()
        .timestamp(LocalDateTime.now())
        .success(true)
        .message(message)
        .data(data)
        .path(path)
        .build();
  }

  public static <T> RespuestaApi<T> error(String message, T data, String path) {
    return RespuestaApi.<T>builder()
        .timestamp(LocalDateTime.now())
        .success(false)
        .message(message)
        .data(data)
        .path(path)
        .build();
  }
}
