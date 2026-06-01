-- Indices recomendados para optimizar listados, dashboard, chatbot y autenticacion.
-- Ejecutar en la base de datos MySQL de AquaComunidad.
-- El script es idempotente: solo crea cada indice si no existe.

DELIMITER $$

DROP PROCEDURE IF EXISTS add_index_if_missing $$
CREATE PROCEDURE add_index_if_missing(
  IN p_schema_name VARCHAR(128),
  IN p_table_name VARCHAR(128),
  IN p_index_name VARCHAR(128),
  IN p_create_sql TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = p_schema_name
      AND table_name = p_table_name
      AND index_name = p_index_name
  ) THEN
    SET @ddl = p_create_sql;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;

CALL add_index_if_missing(
  DATABASE(),
  'reporte',
  'idx_reporte_usuario_fecha',
  'CREATE INDEX idx_reporte_usuario_fecha ON reporte (id_usuario, fecha_creacion DESC)'
);

CALL add_index_if_missing(
  DATABASE(),
  'reporte',
  'idx_reporte_usuario_estado_fecha',
  'CREATE INDEX idx_reporte_usuario_estado_fecha ON reporte (id_usuario, estado, fecha_creacion DESC)'
);

CALL add_index_if_missing(
  DATABASE(),
  'reporte',
  'idx_reporte_estado_fecha',
  'CREATE INDEX idx_reporte_estado_fecha ON reporte (estado, fecha_creacion DESC)'
);

CALL add_index_if_missing(
  DATABASE(),
  'reporte',
  'idx_reporte_tipo_zona_fecha',
  'CREATE INDEX idx_reporte_tipo_zona_fecha ON reporte (id_tipo, zona, fecha_creacion DESC)'
);

CALL add_index_if_missing(
  DATABASE(),
  'reporte',
  'idx_reporte_zona_fecha',
  'CREATE INDEX idx_reporte_zona_fecha ON reporte (zona, fecha_creacion DESC)'
);

CALL add_index_if_missing(
  DATABASE(),
  'caso_operativo',
  'idx_caso_responsable_estado_fecha',
  'CREATE INDEX idx_caso_responsable_estado_fecha ON caso_operativo (id_responsable, estado, fecha_asignacion DESC)'
);

CALL add_index_if_missing(
  DATABASE(),
  'token_recuperacion_contrasena',
  'idx_token_recuperacion_usuario_estado_creado',
  'CREATE INDEX idx_token_recuperacion_usuario_estado_creado ON token_recuperacion_contrasena (id_usuario, estado, creado_en DESC)'
);

CALL add_index_if_missing(
  DATABASE(),
  'catalogo_tipo_incidencia',
  'idx_catalogo_tipo_nombre',
  'CREATE INDEX idx_catalogo_tipo_nombre ON catalogo_tipo_incidencia (nombre)'
);

DROP PROCEDURE IF EXISTS add_index_if_missing;

ANALYZE TABLE
  reporte,
  caso_operativo,
  token_recuperacion_contrasena,
  catalogo_tipo_incidencia;
