package com.aquacomunidad.backend.features.upload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.aquacomunidad.backend.exception.ExcepcionApi;

class ArchivoServicioTest {

  @TempDir
  Path uploadRoot;

  @Test
  void guardarUsaUrlPublicaConfiguradaYAlmacenamientoRemoto() throws Exception {
    AlmacenamientoEnMemoria almacenamiento = new AlmacenamientoEnMemoria();
    ArchivoServicio servicio = new ArchivoServicio(uploadRoot.toString(), "cwebp", 86,
        (source, target, quality) -> Files.writeString(target, "webp-remoto"),
        almacenamiento,
        "https://proyectoutp.com/uploads");
    MockMultipartFile archivo = new MockMultipartFile(
        "file",
        "Tuberia rota.png",
        "image/png",
        "contenido-png".getBytes());

    var respuesta = servicio.guardar(archivo, "reportes");

    String url = respuesta.getArchivos().get(0).getUrl();
    assertThat(url).startsWith("https://proyectoutp.com/uploads/reportes/");
    assertThat(url).endsWith(".webp");
    assertThat(almacenamiento.guardados).hasSize(1);
    assertThat(almacenamiento.guardados.get(0).rutaRelativa()).startsWith("reportes/");
    assertThat(almacenamiento.guardados.get(0).contenido()).isEqualTo("webp-remoto");
  }

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

  private static class AlmacenamientoEnMemoria implements ArchivoAlmacenamiento {

    private final List<Guardado> guardados = new ArrayList<>();

    @Override
    public void guardar(Path archivoLocal, String rutaRelativa) throws Exception {
      guardados.add(new Guardado(rutaRelativa, Files.readString(archivoLocal)));
    }
  }

  private record Guardado(String rutaRelativa, String contenido) {
  }
}
