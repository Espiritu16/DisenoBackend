-- Datos demo para el dashboard administrativo final.
-- Semilla deterministica: 50 ciudadanos, 120 reportes, casos e historial.

INSERT INTO catalogo_tipo_incidencia (codigo, nombre, activo)
SELECT 'FUGA_AGUA', 'Fuga de agua', 1
WHERE NOT EXISTS (SELECT 1 FROM catalogo_tipo_incidencia WHERE codigo = 'FUGA_AGUA');

INSERT INTO catalogo_tipo_incidencia (codigo, nombre, activo)
SELECT 'BAJA_PRESION', 'Baja presión', 1
WHERE NOT EXISTS (SELECT 1 FROM catalogo_tipo_incidencia WHERE codigo = 'BAJA_PRESION');

INSERT INTO catalogo_tipo_incidencia (codigo, nombre, activo)
SELECT 'CORTE_SERVICIO', 'Corte de servicio', 1
WHERE NOT EXISTS (SELECT 1 FROM catalogo_tipo_incidencia WHERE codigo = 'CORTE_SERVICIO');

INSERT INTO catalogo_tipo_incidencia (codigo, nombre, activo)
SELECT 'AGUA_TURBIA', 'Agua turbia', 1
WHERE NOT EXISTS (SELECT 1 FROM catalogo_tipo_incidencia WHERE codigo = 'AGUA_TURBIA');

INSERT INTO catalogo_tipo_incidencia (codigo, nombre, activo)
SELECT 'ALCANTARILLADO', 'Alcantarillado', 1
WHERE NOT EXISTS (SELECT 1 FROM catalogo_tipo_incidencia WHERE codigo = 'ALCANTARILLADO');

INSERT INTO usuario (nombre, correo, password_hash, rol, estado, telefono)
SELECT 'Administrador Dashboard Demo',
       'admin.dashboard@aquacomunidad.test',
       '$2a$10$y0Cug6nFZUHMac8l27Iyn.i1ClsRroFnbvpU.memKq6vlmEP7wPpG',
       'ADMIN',
       'ACTIVO',
       '900000001'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE correo = 'admin.dashboard@aquacomunidad.test');

INSERT INTO usuario (nombre, correo, password_hash, rol, estado, telefono)
SELECT 'Operador Dashboard Demo',
       'operador.dashboard@aquacomunidad.test',
       '$2a$10$y0Cug6nFZUHMac8l27Iyn.i1ClsRroFnbvpU.memKq6vlmEP7wPpG',
       'OPERADOR',
       'ACTIVO',
       '900000002'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE correo = 'operador.dashboard@aquacomunidad.test');

SET @dashboard_seed_admin_id = (
  SELECT id_usuario
  FROM usuario
  WHERE rol = 'ADMIN' AND estado = 'ACTIVO'
  ORDER BY id_usuario
  LIMIT 1
);

SET @dashboard_seed_operador_id = (
  SELECT id_usuario
  FROM usuario
  WHERE rol = 'OPERADOR' AND estado = 'ACTIVO'
  ORDER BY id_usuario
  LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS seed_dashboard_n;
CREATE TEMPORARY TABLE seed_dashboard_n (
  n int NOT NULL PRIMARY KEY
);

INSERT INTO seed_dashboard_n (n) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),
(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),
(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),
(51),(52),(53),(54),(55),(56),(57),(58),(59),(60),
(61),(62),(63),(64),(65),(66),(67),(68),(69),(70),
(71),(72),(73),(74),(75),(76),(77),(78),(79),(80),
(81),(82),(83),(84),(85),(86),(87),(88),(89),(90),
(91),(92),(93),(94),(95),(96),(97),(98),(99),(100),
(101),(102),(103),(104),(105),(106),(107),(108),(109),(110),
(111),(112),(113),(114),(115),(116),(117),(118),(119),(120);

INSERT INTO usuario (nombre, correo, password_hash, rol, estado, telefono)
SELECT CONCAT('Ciudadano Demo ', LPAD(n, 2, '0')),
       CONCAT('ciudadano.dashboard', LPAD(n, 2, '0'), '@aquacomunidad.test'),
       '$2a$10$y0Cug6nFZUHMac8l27Iyn.i1ClsRroFnbvpU.memKq6vlmEP7wPpG',
       'CIUDADANO',
       'ACTIVO',
       CONCAT('91', LPAD(n, 7, '0'))
FROM seed_dashboard_n
WHERE n <= 50
  AND NOT EXISTS (
    SELECT 1
    FROM usuario u
    WHERE u.correo = CONCAT('ciudadano.dashboard', LPAD(seed_dashboard_n.n, 2, '0'), '@aquacomunidad.test')
  );

DROP TEMPORARY TABLE IF EXISTS seed_dashboard_reportes;
CREATE TEMPORARY TABLE seed_dashboard_reportes AS
SELECT n,
       MOD(n - 1, 50) + 1 AS ciudadano_num,
       CONCAT('ciudadano.dashboard', LPAD(MOD(n - 1, 50) + 1, 2, '0'), '@aquacomunidad.test') AS correo_ciudadano,
       CASE MOD(n, 10)
         WHEN 1 THEN 'San Miguel'
         WHEN 2 THEN 'Ate'
         WHEN 3 THEN 'San Juan de Lurigancho'
         WHEN 4 THEN 'Villa El Salvador'
         WHEN 5 THEN 'Comas'
         WHEN 6 THEN 'Cercado de Lima'
         WHEN 7 THEN 'Santiago de Surco'
         WHEN 8 THEN 'Miraflores'
         WHEN 9 THEN 'Callao'
         ELSE 'Los Olivos'
       END AS zona,
       CASE MOD(n, 5)
         WHEN 1 THEN 'FUGA_AGUA'
         WHEN 2 THEN 'BAJA_PRESION'
         WHEN 3 THEN 'CORTE_SERVICIO'
         WHEN 4 THEN 'AGUA_TURBIA'
         ELSE 'ALCANTARILLADO'
       END AS tipo_codigo,
       CASE
         WHEN MOD(n, 10) IN (1, 2, 3) THEN 'PENDIENTE'
         WHEN MOD(n, 10) IN (4, 5) THEN 'EN_PROCESO'
         WHEN MOD(n, 10) IN (6, 7, 8) THEN 'RESUELTO'
         WHEN MOD(n, 10) = 9 THEN 'ESCALADO'
         ELSE 'RECHAZADO'
       END AS estado,
       DATE_ADD(DATE_ADD('2026-07-01 08:00:00', INTERVAL MOD(n - 1, 92) DAY), INTERVAL MOD(n, 9) HOUR) AS fecha_base,
       CASE MOD(n, 10)
         WHEN 1 THEN -12.0763000
         WHEN 2 THEN -12.0267000
         WHEN 3 THEN -11.9817000
         WHEN 4 THEN -12.2130000
         WHEN 5 THEN -11.9431000
         WHEN 6 THEN -12.0464000
         WHEN 7 THEN -12.1111000
         WHEN 8 THEN -12.1211000
         WHEN 9 THEN -12.0500000
         ELSE -11.9581000
       END + (MOD(n, 7) * 0.0003000) AS lat,
       CASE MOD(n, 10)
         WHEN 1 THEN -77.0823000
         WHEN 2 THEN -76.9194000
         WHEN 3 THEN -77.0069000
         WHEN 4 THEN -76.9370000
         WHEN 5 THEN -77.0489000
         WHEN 6 THEN -77.0428000
         WHEN 7 THEN -76.9919000
         WHEN 8 THEN -77.0297000
         WHEN 9 THEN -77.1186000
         ELSE -77.0746000
       END - (MOD(n, 6) * 0.0003000) AS lng
FROM seed_dashboard_n;

INSERT INTO reporte (
  id_usuario,
  id_tipo,
  descripcion,
  foto_url,
  lat,
  lng,
  direccion,
  zona,
  estado,
  posible_duplicado,
  fecha_creacion,
  fecha_actualizacion
)
SELECT u.id_usuario,
       t.id_tipo,
       CONCAT('Reporte demo ', LPAD(d.n, 3, '0'), ': ',
              LOWER(t.nombre), ' detectado en ', d.zona,
              '. Registro sembrado para alimentar el dashboard administrativo.'),
       CONCAT('https://demo.aquacomunidad.test/reportes/reporte-', LPAD(d.n, 3, '0'), '.jpg'),
       d.lat,
       d.lng,
       CONCAT('Av. Principal ', d.n, ', ', d.zona),
       d.zona,
       d.estado,
       CASE WHEN MOD(d.n, 13) = 0 THEN 1 ELSE 0 END,
       d.fecha_base,
       CASE
         WHEN d.estado = 'PENDIENTE' THEN d.fecha_base
         WHEN d.estado = 'EN_PROCESO' THEN DATE_ADD(d.fecha_base, INTERVAL 8 HOUR)
         ELSE DATE_ADD(d.fecha_base, INTERVAL 2 DAY)
       END
FROM seed_dashboard_reportes d
JOIN usuario u ON u.correo = d.correo_ciudadano
JOIN catalogo_tipo_incidencia t ON t.codigo = d.tipo_codigo
WHERE NOT EXISTS (
  SELECT 1
  FROM reporte r
  JOIN usuario ud ON ud.id_usuario = r.id_usuario
  WHERE ud.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
);

INSERT INTO reporte_imagen (id_reporte, url, public_id, orden, fecha_creacion)
SELECT r.id_reporte,
       r.foto_url,
       CONCAT('dashboard-demo/reporte-', r.id_reporte),
       0,
       r.fecha_creacion
FROM reporte r
JOIN usuario u ON u.id_usuario = r.id_usuario
WHERE u.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
  AND NOT EXISTS (
    SELECT 1
    FROM reporte_imagen ri
    WHERE ri.id_reporte = r.id_reporte
  );

INSERT INTO caso_operativo (
  id_reporte_origen,
  id_responsable,
  prioridad,
  estado,
  observaciones,
  evidencia_cierre,
  fecha_asignacion,
  fecha_cierre,
  creado_por
)
SELECT r.id_reporte,
       @dashboard_seed_operador_id,
       CASE
         WHEN r.estado IN ('ESCALADO', 'RECHAZADO') THEN 'ALTA'
         WHEN r.estado = 'RESUELTO' THEN 'MEDIA'
         ELSE 'BAJA'
       END,
       CASE
         WHEN r.estado = 'RESUELTO' THEN 'RESUELTO'
         WHEN r.estado = 'ESCALADO' THEN 'ESCALADO'
         WHEN r.estado = 'RECHAZADO' THEN 'RECHAZADO'
         ELSE 'EN_PROCESO'
       END,
       CONCAT('Caso demo generado desde el reporte ', r.id_reporte, ' para trazabilidad del dashboard.'),
       CASE
         WHEN r.estado = 'RESUELTO' THEN CONCAT('https://demo.aquacomunidad.test/evidencias/cierre-', r.id_reporte, '.jpg')
         ELSE NULL
       END,
       DATE_ADD(r.fecha_creacion, INTERVAL 8 HOUR),
       CASE
         WHEN r.estado IN ('RESUELTO', 'ESCALADO', 'RECHAZADO') THEN DATE_ADD(r.fecha_creacion, INTERVAL 2 DAY)
         ELSE NULL
       END,
       @dashboard_seed_admin_id
FROM reporte r
JOIN usuario u ON u.id_usuario = r.id_usuario
WHERE u.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
  AND r.estado <> 'PENDIENTE'
  AND @dashboard_seed_admin_id IS NOT NULL
  AND @dashboard_seed_operador_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM caso_operativo c
    WHERE c.id_reporte_origen = r.id_reporte
  );

INSERT INTO caso_evidencia (id_caso, url, public_id, orden, fecha_creacion)
SELECT c.id_caso,
       c.evidencia_cierre,
       CONCAT('dashboard-demo/cierre-', c.id_caso),
       0,
       c.fecha_cierre
FROM caso_operativo c
JOIN reporte r ON r.id_reporte = c.id_reporte_origen
JOIN usuario u ON u.id_usuario = r.id_usuario
WHERE u.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
  AND c.estado = 'RESUELTO'
  AND c.evidencia_cierre IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM caso_evidencia ce
    WHERE ce.id_caso = c.id_caso
  );

INSERT INTO historial_estado_reporte (
  id_reporte,
  estado_anterior,
  estado_nuevo,
  observacion,
  cambiado_por,
  fecha_cambio
)
SELECT r.id_reporte,
       NULL,
       'PENDIENTE',
       'Semilla dashboard: reporte registrado por ciudadano.',
       r.id_usuario,
       r.fecha_creacion
FROM reporte r
JOIN usuario u ON u.id_usuario = r.id_usuario
WHERE u.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
  AND NOT EXISTS (
    SELECT 1
    FROM historial_estado_reporte h
    WHERE h.id_reporte = r.id_reporte
      AND h.observacion = 'Semilla dashboard: reporte registrado por ciudadano.'
  );

INSERT INTO historial_estado_reporte (
  id_reporte,
  estado_anterior,
  estado_nuevo,
  observacion,
  cambiado_por,
  fecha_cambio
)
SELECT r.id_reporte,
       'PENDIENTE',
       'EN_PROCESO',
       'Semilla dashboard: caso derivado a operador.',
       @dashboard_seed_admin_id,
       DATE_ADD(r.fecha_creacion, INTERVAL 8 HOUR)
FROM reporte r
JOIN usuario u ON u.id_usuario = r.id_usuario
WHERE u.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
  AND r.estado <> 'PENDIENTE'
  AND @dashboard_seed_admin_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM historial_estado_reporte h
    WHERE h.id_reporte = r.id_reporte
      AND h.observacion = 'Semilla dashboard: caso derivado a operador.'
  );

INSERT INTO historial_estado_reporte (
  id_reporte,
  estado_anterior,
  estado_nuevo,
  observacion,
  cambiado_por,
  fecha_cambio
)
SELECT r.id_reporte,
       'EN_PROCESO',
       r.estado,
       CONCAT('Semilla dashboard: reporte marcado como ', LOWER(r.estado), '.'),
       @dashboard_seed_operador_id,
       DATE_ADD(r.fecha_creacion, INTERVAL 2 DAY)
FROM reporte r
JOIN usuario u ON u.id_usuario = r.id_usuario
WHERE u.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
  AND r.estado IN ('RESUELTO', 'ESCALADO', 'RECHAZADO')
  AND @dashboard_seed_operador_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM historial_estado_reporte h
    WHERE h.id_reporte = r.id_reporte
      AND h.observacion = CONCAT('Semilla dashboard: reporte marcado como ', LOWER(r.estado), '.')
  );

INSERT INTO historial_estado_caso (
  id_caso,
  estado_anterior,
  estado_nuevo,
  observacion,
  cambiado_por,
  fecha_cambio
)
SELECT c.id_caso,
       NULL,
       'EN_PROCESO',
       'Semilla dashboard: caso creado para atencion operativa.',
       @dashboard_seed_admin_id,
       c.fecha_asignacion
FROM caso_operativo c
JOIN reporte r ON r.id_reporte = c.id_reporte_origen
JOIN usuario u ON u.id_usuario = r.id_usuario
WHERE u.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
  AND @dashboard_seed_admin_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM historial_estado_caso h
    WHERE h.id_caso = c.id_caso
      AND h.observacion = 'Semilla dashboard: caso creado para atencion operativa.'
  );

INSERT INTO historial_estado_caso (
  id_caso,
  estado_anterior,
  estado_nuevo,
  observacion,
  cambiado_por,
  fecha_cambio
)
SELECT c.id_caso,
       'EN_PROCESO',
       c.estado,
       CONCAT('Semilla dashboard: caso marcado como ', LOWER(c.estado), '.'),
       @dashboard_seed_operador_id,
       c.fecha_cierre
FROM caso_operativo c
JOIN reporte r ON r.id_reporte = c.id_reporte_origen
JOIN usuario u ON u.id_usuario = r.id_usuario
WHERE u.correo LIKE 'ciudadano.dashboard%@aquacomunidad.test'
  AND c.estado IN ('RESUELTO', 'ESCALADO', 'RECHAZADO')
  AND c.fecha_cierre IS NOT NULL
  AND @dashboard_seed_operador_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM historial_estado_caso h
    WHERE h.id_caso = c.id_caso
      AND h.observacion = CONCAT('Semilla dashboard: caso marcado como ', LOWER(c.estado), '.')
  );

DROP TEMPORARY TABLE IF EXISTS seed_dashboard_reportes;
DROP TEMPORARY TABLE IF EXISTS seed_dashboard_n;
