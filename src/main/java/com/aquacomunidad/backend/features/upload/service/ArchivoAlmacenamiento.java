package com.aquacomunidad.backend.features.upload.service;

import java.nio.file.Path;

public interface ArchivoAlmacenamiento {

  void guardar(Path archivoLocal, String rutaRelativa) throws Exception;
}
