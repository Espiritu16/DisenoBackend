-- Normaliza el tipo con codificacion corrupta "Baja presiÃ³n" hacia "Baja presión".
-- Si existe un catalogo duplicado, los reportes se reasignan al codigo canonico BAJA_PRESION.

INSERT INTO catalogo_tipo_incidencia (codigo, nombre, activo)
SELECT 'BAJA_PRESION', 'Baja presión', 1
WHERE NOT EXISTS (
  SELECT 1
  FROM catalogo_tipo_incidencia
  WHERE codigo = 'BAJA_PRESION'
);

UPDATE catalogo_tipo_incidencia
SET nombre = 'Baja presión',
    activo = 1
WHERE codigo = 'BAJA_PRESION';

SET @tipo_baja_presion_id = (
  SELECT id_tipo
  FROM catalogo_tipo_incidencia
  WHERE codigo = 'BAJA_PRESION'
  LIMIT 1
);

UPDATE reporte r
JOIN catalogo_tipo_incidencia t ON t.id_tipo = r.id_tipo
SET r.id_tipo = @tipo_baja_presion_id
WHERE @tipo_baja_presion_id IS NOT NULL
  AND t.id_tipo <> @tipo_baja_presion_id
  AND LOWER(TRIM(t.nombre)) IN ('baja presiÃ³n', 'baja presión', 'baja presion');

UPDATE catalogo_tipo_incidencia
SET activo = 0
WHERE @tipo_baja_presion_id IS NOT NULL
  AND id_tipo <> @tipo_baja_presion_id
  AND LOWER(TRIM(nombre)) IN ('baja presiÃ³n', 'baja presión', 'baja presion');

UPDATE catalogo_tipo_incidencia
SET nombre = 'Baja presión'
WHERE LOWER(TRIM(nombre)) IN ('baja presiÃ³n', 'baja presion');
