package com.aquacomunidad.backend.features.upload.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

  private final Path uploadRoot;
  private final int webpQuality;
  private final WebpConverter webpConverter;

  @Autowired
  public ArchivoServicio(
      @Value("${app.upload.dir:${UPLOAD_DIR:/app/uploads}}") String uploadDir,
      @Value("${app.upload.webp-command:cwebp}") String webpCommand,
      @Value("${app.upload.webp-quality:86}") int webpQuality) {
    this(uploadDir, webpCommand, webpQuality, new CwebpConverter(webpCommand));
  }

  ArchivoServicio(String uploadDir, String webpCommand, int webpQuality, WebpConverter webpConverter) {
    this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    this.webpQuality = Math.max(1, Math.min(webpQuality, 100));
    this.webpConverter = webpConverter;
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
    return guardarLocalWebp(file, carpetaNormalizada, publicId, original);
  }

  private ArchivoSubidoItemDto guardarLocalWebp(MultipartFile file, String carpeta, String publicId, String original) {
    Path temporal = null;
    try {
      String extension = extensionDe(original);
      String fileName = publicId + ".webp";
      Path targetDir = uploadRoot.resolve(carpeta).normalize();
      Files.createDirectories(targetDir);
      Path target = targetDir.resolve(fileName).normalize();
      if (!target.startsWith(targetDir)) {
        throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Nombre de archivo invalido");
      }
      if (".webp".equals(extension)) {
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
      } else {
        temporal = Files.createTempFile(targetDir, publicId + "-", extension);
        Files.copy(file.getInputStream(), temporal, StandardCopyOption.REPLACE_EXISTING);
        webpConverter.convert(temporal, target, webpQuality);
      }
      return ArchivoSubidoItemDto.builder()
          .url("/uploads/" + carpeta + "/" + fileName)
          .build();
    } catch (IOException ex) {
      throw new ExcepcionApi(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen localmente");
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
