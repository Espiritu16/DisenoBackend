package com.aquacomunidad.backend.features.upload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.aquacomunidad.backend.exception.ExcepcionApi;

class ArchivoServicioTest {

  @TempDir
  Path uploadRoot;

  @Test
  void guardarConvierteImagenAWebpEnDirectorioConfigurado() throws Exception {
    ArchivoServicio servicio = new ArchivoServicio(uploadRoot.toString(), "cwebp", 86,
        (source, target, quality) -> Files.writeString(target, "webp-convertido"));
    MockMultipartFile archivo = new MockMultipartFile(
        "file",
        "Fuga de agua.png",
        "image/png",
        "contenido-png".getBytes());

    var respuesta = servicio.guardar(archivo, "reportes");

    assertThat(respuesta.getArchivos()).hasSize(1);
    String url = respuesta.getArchivos().get(0).getUrl();
    assertThat(url).startsWith("/uploads/reportes/");
    assertThat(url).endsWith(".webp");
    String fileName = url.substring("/uploads/reportes/".length());
    assertThat(uploadRoot.resolve("reportes").resolve(fileName))
        .exists()
        .hasContent("webp-convertido");
  }

  @Test
  void guardarRechazaFormatosNoPermitidos() {
    ArchivoServicio servicio = new ArchivoServicio(uploadRoot.toString(), "cwebp", 86,
        (source, target, quality) -> Files.writeString(target, "no-debe-ejecutarse"));
    MockMultipartFile archivo = new MockMultipartFile(
        "file",
        "documento.pdf",
        "application/pdf",
        "pdf".getBytes());

    ExcepcionApi ex = assertThrows(ExcepcionApi.class, () -> servicio.guardar(archivo, "reportes"));

    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
