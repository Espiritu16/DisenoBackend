package com.aquacomunidad.backend.features.upload.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.upload.dto.ArchivoSubidoItemDto;
import com.aquacomunidad.backend.features.upload.dto.ArchivoSubidoRespuestaDto;

@Service
public class ArchivoServicio {

  private static final long MAX_BYTES = 10L * 1024 * 1024;
  private static final Set<String> MIME_PERMITIDOS = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
  private static final Set<String> CARPETAS_PERMITIDAS = Set.of("reportes", "casos");

  private final Path tempRoot;
  private final int webpQuality;
  private final WebpConverter webpConverter;
  private final ArchivoAlmacenamiento almacenamiento;
  private final String publicBaseUrl;

  @Autowired
  public ArchivoServicio(
      @Value("${app.upload.dir:${UPLOAD_DIR:/app/uploads}}") String uploadDir,
      @Value("${app.upload.webp-command:cwebp}") String webpCommand,
      @Value("${app.upload.webp-quality:86}") int webpQuality,
      @Value("${app.upload.public-base-url:${UPLOAD_PUBLIC_BASE_URL:/uploads}}") String publicBaseUrl,
      ArchivoAlmacenamiento almacenamiento) {
    this(uploadDir, webpCommand, webpQuality, new CwebpConverter(webpCommand), almacenamiento, publicBaseUrl);
  }

  ArchivoServicio(String uploadDir, String webpCommand, int webpQuality, WebpConverter webpConverter) {
    this(uploadDir, webpCommand, webpQuality, webpConverter, new ArchivoAlmacenamientoLocal(uploadDir), "/uploads");
  }

  ArchivoServicio(
      String uploadDir,
      String webpCommand,
      int webpQuality,
      WebpConverter webpConverter,
      ArchivoAlmacenamiento almacenamiento,
      String publicBaseUrl) {
    this.tempRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    this.webpQuality = Math.max(1, Math.min(webpQuality, 100));
    this.webpConverter = webpConverter;
    this.almacenamiento = almacenamiento;
    this.publicBaseUrl = limpiarBaseUrl(publicBaseUrl);
  }

  public ArchivoSubidoRespuestaDto guardar(MultipartFile file, String carpeta) {
    ArchivoSubidoItemDto item = subirUno(file, carpeta);
    return ArchivoSubidoRespuestaDto.builder()
        .archivos(List.of(item))
        .build();
  }

  public ArchivoSubidoRespuestaDto guardarTodos(List<MultipartFile> files, String carpeta) {
    if (files == null || files.isEmpty()) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Debes seleccionar al menos una imagen");
    }
    List<ArchivoSubidoItemDto> items = files.stream()
        .map(file -> subirUno(file, carpeta))
        .toList();
    return ArchivoSubidoRespuestaDto.builder()
        .archivos(items)
        .build();
  }

  private ArchivoSubidoItemDto subirUno(MultipartFile file, String carpeta) {
    validar(file);
    String carpetaNormalizada = validarCarpeta(carpeta);
    String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "archivo" : file.getOriginalFilename());
    String nombreBase = sanitizar(original.replaceFirst("\\.[^.]+$", ""));
    String publicId = UUID.randomUUID() + "-" + nombreBase;
    return guardarWebp(file, carpetaNormalizada, publicId, original);
  }

  private ArchivoSubidoItemDto guardarWebp(MultipartFile file, String carpeta, String publicId, String original) {
    Path temporal = null;
    try {
      String extension = extensionDe(original);
      String fileName = publicId + ".webp";
      String rutaRelativa = carpeta + "/" + fileName;
      Files.createDirectories(tempRoot);
      temporal = Files.createTempFile(tempRoot, publicId + "-", ".webp");
      if (".webp".equals(extension)) {
        file.transferTo(temporal);
      } else {
        Path originalTemporal = Files.createTempFile(tempRoot, publicId + "-original-", extension);
        try {
          file.transferTo(originalTemporal);
          webpConverter.convert(originalTemporal, temporal, webpQuality);
        } finally {
          Files.deleteIfExists(originalTemporal);
        }
      }
      almacenamiento.guardar(temporal, rutaRelativa);
      return ArchivoSubidoItemDto.builder()
          .url(publicBaseUrl + "/" + rutaRelativa)
          .build();
    } catch (Exception ex) {
      throw new ExcepcionApi(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen");
    } finally {
      if (temporal != null) {
        try {
          Files.deleteIfExists(temporal);
        } catch (IOException ignored) {
          // No bloquea la subida si solo falla la limpieza del temporal.
        }
      }
    }
  }

  private String extensionDe(String original) {
    int index = original.lastIndexOf('.');
    if (index < 0 || index == original.length() - 1) {
      return ".jpg";
    }
    String extension = original.substring(index).toLowerCase();
    return extension.matches("\\.(jpg|jpeg|png|webp)") ? extension : ".jpg";
  }

  private void validar(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "El archivo es obligatorio");
    }
    if (file.getSize() > MAX_BYTES) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Cada imagen no debe superar 10MB");
    }
    String contentType = file.getContentType();
    if (contentType == null || !MIME_PERMITIDOS.contains(contentType.toLowerCase())) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Solo se permiten imagenes JPG, PNG o WEBP");
    }
  }

  private String validarCarpeta(String carpeta) {
    String normalizada = sanitizar(carpeta);
    if (!CARPETAS_PERMITIDAS.contains(normalizada)) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Carpeta de subida no permitida");
    }
    return normalizada;
  }

  private String sanitizar(String value) {
    String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replaceAll("[^a-zA-Z0-9_-]", "-")
        .replaceAll("-+", "-")
        .replaceAll("(^-|-$)", "")
        .toLowerCase();
    return normalized.isBlank() ? "archivo" : normalized;
  }

  private String limpiarBaseUrl(String value) {
    String base = value == null || value.isBlank() ? "/uploads" : value.trim();
    while (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base;
  }

  @FunctionalInterface
  interface WebpConverter {
    void convert(Path source, Path target, int quality) throws IOException;
  }

  private static class CwebpConverter implements WebpConverter {
    private final String command;

    CwebpConverter(String command) {
      this.command = command == null || command.isBlank() ? "cwebp" : command;
    }

    @Override
    public void convert(Path source, Path target, int quality) throws IOException {
      ProcessBuilder processBuilder = new ProcessBuilder(
          command,
          "-quiet",
          "-q",
          String.valueOf(quality),
          source.toString(),
          "-o",
          target.toString());
      Process process = processBuilder.start();
      try {
        int exitCode = process.waitFor();
        if (exitCode != 0) {
          throw new IOException("cwebp termino con codigo " + exitCode);
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new IOException("Conversion WebP interrumpida", ex);
      }
    }
  }
}
