-- Normaliza el tipo historico "Fuga" hacia "Fuga de Agua".
-- Si existe un catalogo duplicado, los reportes se reasignan al codigo canonico FUGA_AGUA.

INSERT INTO catalogo_tipo_incidencia (codigo, nombre, activo)
SELECT 'FUGA_AGUA', 'Fuga de Agua', 1
WHERE NOT EXISTS (
  SELECT 1
  FROM catalogo_tipo_incidencia
  WHERE codigo = 'FUGA_AGUA'
);

UPDATE catalogo_tipo_incidencia
SET nombre = 'Fuga de Agua',
    activo = 1
WHERE codigo = 'FUGA_AGUA';

SET @tipo_fuga_agua_id = (
  SELECT id_tipo
  FROM catalogo_tipo_incidencia
  WHERE codigo = 'FUGA_AGUA'
  LIMIT 1
);

UPDATE reporte r
JOIN catalogo_tipo_incidencia t ON t.id_tipo = r.id_tipo
SET r.id_tipo = @tipo_fuga_agua_id
WHERE @tipo_fuga_agua_id IS NOT NULL
  AND t.id_tipo <> @tipo_fuga_agua_id
  AND LOWER(TRIM(t.nombre)) = 'fuga';

UPDATE catalogo_tipo_incidencia
SET activo = 0
WHERE @tipo_fuga_agua_id IS NOT NULL
  AND id_tipo <> @tipo_fuga_agua_id
  AND LOWER(TRIM(nombre)) = 'fuga';

UPDATE catalogo_tipo_incidencia
SET nombre = 'Fuga de Agua'
WHERE LOWER(TRIM(nombre)) = 'fuga de agua';
