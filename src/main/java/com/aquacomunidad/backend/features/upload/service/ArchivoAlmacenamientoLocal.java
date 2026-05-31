package com.aquacomunidad.backend.features.upload.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.upload.storage", havingValue = "local", matchIfMissing = true)
public class ArchivoAlmacenamientoLocal implements ArchivoAlmacenamiento {

  private final Path uploadRoot;

  public ArchivoAlmacenamientoLocal(@Value("${app.upload.dir:${UPLOAD_DIR:/app/uploads}}") String uploadDir) {
    this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  @Override
  public void guardar(Path archivoLocal, String rutaRelativa) throws Exception {
    Path target = uploadRoot.resolve(rutaRelativa).normalize();
    if (!target.startsWith(uploadRoot)) {
      throw new IllegalArgumentException("Ruta de archivo invalida");
    }
    Files.createDirectories(target.getParent());
    Files.copy(archivoLocal, target, StandardCopyOption.REPLACE_EXISTING);
  }
}
