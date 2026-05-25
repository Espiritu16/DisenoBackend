package com.aquacomunidad.backend.features.upload.service;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.upload.dto.ArchivoSubidoItemDto;
import com.aquacomunidad.backend.features.upload.dto.ArchivoSubidoRespuestaDto;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class ArchivoServicio {

  private static final long MAX_BYTES = 10L * 1024 * 1024;
  private static final Set<String> MIME_PERMITIDOS = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

  private final Cloudinary cloudinary;

  public ArchivoServicio(
      @Value("${cloudinary.cloud-name}") String cloudName,
      @Value("${cloudinary.api-key}") String apiKey,
      @Value("${cloudinary.api-secret}") String apiSecret) {
    this.cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", cloudName,
        "api_key", apiKey,
        "api_secret", apiSecret,
        "secure", true));
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
    String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "archivo" : file.getOriginalFilename());
    String nombreBase = sanitizar(original.replaceFirst("\\.[^.]+$", ""));
    String publicId = UUID.randomUUID() + "-" + nombreBase;
    try {
      Map<?, ?> response = cloudinary.uploader().upload(
          file.getBytes(),
          ObjectUtils.asMap(
              "folder", "aquacomunidad/" + carpeta,
              "public_id", publicId,
              "overwrite", true,
              "resource_type", "image"));
      Object secureUrl = response.get("secure_url");
      Object publicIdValue = response.get("public_id");
      if (secureUrl == null) {
        throw new ExcepcionApi(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary no devolvio URL segura");
      }
      return ArchivoSubidoItemDto.builder()
          .url(secureUrl.toString())
          .build();
    } catch (IOException ex) {
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen");
    } catch (ExcepcionApi ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ExcepcionApi(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo subir la imagen a Cloudinary");
    }
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
      throw new ExcepcionApi(HttpStatus.BAD_REQUEST, "Solo se permiten imagenes JPG, PNG, WEBP o GIF");
    }
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
}
