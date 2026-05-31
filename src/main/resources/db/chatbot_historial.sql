USE aquacomunidad_db;

CREATE TABLE IF NOT EXISTS chatbot_conversacion (
  id_conversacion BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario BIGINT UNSIGNED NOT NULL,
  fecha_conversacion DATE NOT NULL,
  titulo VARCHAR(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_conversacion),
  UNIQUE KEY uq_chatbot_conversacion_usuario_fecha (id_usuario, fecha_conversacion),
  KEY idx_chatbot_conversacion_usuario_actualizado (id_usuario, actualizado_en),
  CONSTRAINT fk_chatbot_conversacion_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chatbot_mensaje (
  id_mensaje BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_conversacion BIGINT UNSIGNED NOT NULL,
  rol ENUM('USUARIO','ASISTENTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  contenido VARCHAR(2500) COLLATE utf8mb4_unicode_ci NOT NULL,
  proveedor VARCHAR(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  modelo VARCHAR(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  ia_disponible TINYINT(1) NOT NULL DEFAULT 0,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_mensaje),
  KEY idx_chatbot_mensaje_conversacion_fecha (id_conversacion, creado_en),
  CONSTRAINT fk_chatbot_mensaje_conversacion
    FOREIGN KEY (id_conversacion) REFERENCES chatbot_conversacion (id_conversacion)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE chatbot_mensaje
  MODIFY rol ENUM('USER','ASSISTANT','USUARIO','ASISTENTE') COLLATE utf8mb4_unicode_ci NOT NULL;

UPDATE chatbot_mensaje SET rol = 'USUARIO' WHERE rol = 'USER';
UPDATE chatbot_mensaje SET rol = 'ASISTENTE' WHERE rol = 'ASSISTANT';

ALTER TABLE chatbot_mensaje
  MODIFY rol ENUM('USUARIO','ASISTENTE') COLLATE utf8mb4_unicode_ci NOT NULL;
