-- MVP 1/2/3: estado del servicio, rol autoridad e IoT basico.
-- Ejecutar despues del esquema base existente de AquaComunidad.

ALTER TABLE usuario
  MODIFY rol enum('CIUDADANO','ADMIN','OPERADOR','AUTORIDAD') COLLATE utf8mb4_unicode_ci NOT NULL;

CREATE TABLE IF NOT EXISTS zona_servicio (
  id_zona bigint unsigned NOT NULL AUTO_INCREMENT,
  nombre varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  codigo varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  activo tinyint(1) NOT NULL DEFAULT '1',
  creado_en datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_zona),
  UNIQUE KEY uq_zona_servicio_nombre (nombre),
  UNIQUE KEY uq_zona_servicio_codigo (codigo),
  KEY idx_zona_servicio_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS alerta_servicio (
  id_alerta bigint unsigned NOT NULL AUTO_INCREMENT,
  id_zona bigint unsigned DEFAULT NULL,
  tipo enum('CORTE_PROGRAMADO','CORTE_NO_PROGRAMADO','MANTENIMIENTO','RIESGO_DESABASTECIMIENTO','INFORMATIVA') COLLATE utf8mb4_unicode_ci NOT NULL,
  titulo varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  descripcion varchar(800) COLLATE utf8mb4_unicode_ci NOT NULL,
  severidad enum('INFO','MEDIA','ALTA','CRITICA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INFO',
  estado enum('PROGRAMADA','ACTIVA','RESUELTA','CANCELADA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVA',
  inicia_en datetime DEFAULT NULL,
  finaliza_en datetime DEFAULT NULL,
  creado_por bigint unsigned NOT NULL,
  creado_en datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_alerta),
  KEY idx_alerta_servicio_zona_estado_fecha (id_zona, estado, inicia_en),
  KEY idx_alerta_servicio_estado_fecha (estado, inicia_en, creado_en),
  KEY idx_alerta_servicio_creado_por (creado_por),
  CONSTRAINT fk_alerta_servicio_zona FOREIGN KEY (id_zona) REFERENCES zona_servicio (id_zona),
  CONSTRAINT fk_alerta_servicio_creado_por FOREIGN KEY (creado_por) REFERENCES usuario (id_usuario),
  CONSTRAINT chk_alerta_servicio_titulo_no_vacio CHECK (char_length(trim(titulo)) > 0),
  CONSTRAINT chk_alerta_servicio_descripcion_no_vacia CHECK (char_length(trim(descripcion)) > 0),
  CONSTRAINT chk_alerta_servicio_fechas CHECK (finaliza_en IS NULL OR inicia_en IS NULL OR finaliza_en >= inicia_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS infraestructura_hidrica (
  id_infraestructura bigint unsigned NOT NULL AUTO_INCREMENT,
  id_zona bigint unsigned NOT NULL,
  nombre varchar(140) COLLATE utf8mb4_unicode_ci NOT NULL,
  tipo enum('TANQUE','RESERVORIO','TUBERIA_PRINCIPAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  capacidad_litros decimal(12,2) DEFAULT NULL,
  lat decimal(10,7) DEFAULT NULL,
  lng decimal(10,7) DEFAULT NULL,
  activo tinyint(1) NOT NULL DEFAULT '1',
  creado_en datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_infraestructura),
  KEY idx_infraestructura_zona_activo (id_zona, activo),
  KEY idx_infraestructura_tipo (tipo),
  CONSTRAINT fk_infraestructura_zona FOREIGN KEY (id_zona) REFERENCES zona_servicio (id_zona),
  CONSTRAINT chk_infraestructura_nombre_no_vacio CHECK (char_length(trim(nombre)) > 0),
  CONSTRAINT chk_infraestructura_lat CHECK (lat IS NULL OR (lat >= -90 AND lat <= 90)),
  CONSTRAINT chk_infraestructura_lng CHECK (lng IS NULL OR (lng >= -180 AND lng <= 180))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lectura_iot (
  id_lectura bigint unsigned NOT NULL AUTO_INCREMENT,
  id_infraestructura bigint unsigned NOT NULL,
  nivel_porcentaje decimal(5,2) NOT NULL,
  volumen_litros decimal(12,2) DEFAULT NULL,
  bateria_porcentaje decimal(5,2) DEFAULT NULL,
  senal_porcentaje decimal(5,2) DEFAULT NULL,
  leido_en datetime NOT NULL,
  recibido_en datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_lectura),
  KEY idx_lectura_iot_infraestructura_fecha (id_infraestructura, leido_en DESC),
  KEY idx_lectura_iot_recibido (recibido_en DESC),
  CONSTRAINT fk_lectura_iot_infraestructura FOREIGN KEY (id_infraestructura) REFERENCES infraestructura_hidrica (id_infraestructura),
  CONSTRAINT chk_lectura_iot_nivel CHECK (nivel_porcentaje >= 0 AND nivel_porcentaje <= 100),
  CONSTRAINT chk_lectura_iot_bateria CHECK (bateria_porcentaje IS NULL OR (bateria_porcentaje >= 0 AND bateria_porcentaje <= 100)),
  CONSTRAINT chk_lectura_iot_senal CHECK (senal_porcentaje IS NULL OR (senal_porcentaje >= 0 AND senal_porcentaje <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS alerta_iot (
  id_alerta_iot bigint unsigned NOT NULL AUTO_INCREMENT,
  id_infraestructura bigint unsigned NOT NULL,
  id_lectura bigint unsigned DEFAULT NULL,
  tipo enum('NIVEL_BAJO','DESCENSO_ANOMALO','SIN_SENAL','BATERIA_BAJA') COLLATE utf8mb4_unicode_ci NOT NULL,
  severidad enum('MEDIA','ALTA','CRITICA') COLLATE utf8mb4_unicode_ci NOT NULL,
  estado enum('ACTIVA','ATENDIDA','DESCARTADA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVA',
  mensaje varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  creada_en datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atendida_en datetime DEFAULT NULL,
  atendida_por bigint unsigned DEFAULT NULL,
  PRIMARY KEY (id_alerta_iot),
  KEY idx_alerta_iot_infraestructura_estado (id_infraestructura, estado, creada_en DESC),
  KEY idx_alerta_iot_estado_tipo (estado, tipo, creada_en DESC),
  KEY idx_alerta_iot_lectura (id_lectura),
  KEY idx_alerta_iot_atendida_por (atendida_por),
  CONSTRAINT fk_alerta_iot_infraestructura FOREIGN KEY (id_infraestructura) REFERENCES infraestructura_hidrica (id_infraestructura),
  CONSTRAINT fk_alerta_iot_lectura FOREIGN KEY (id_lectura) REFERENCES lectura_iot (id_lectura),
  CONSTRAINT fk_alerta_iot_atendida_por FOREIGN KEY (atendida_por) REFERENCES usuario (id_usuario),
  CONSTRAINT chk_alerta_iot_mensaje_no_vacio CHECK (char_length(trim(mensaje)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
