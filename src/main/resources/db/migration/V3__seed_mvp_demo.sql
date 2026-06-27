-- Datos demo para presentar MVP 1/2/3 sin carga manual.
-- Idempotente por codigos/nombres para evitar duplicados si se reintenta en desarrollo.

INSERT INTO zona_servicio (nombre, codigo, activo)
SELECT 'Lima Centro', 'LIMA-CENTRO', 1
WHERE NOT EXISTS (SELECT 1 FROM zona_servicio WHERE codigo = 'LIMA-CENTRO');

INSERT INTO zona_servicio (nombre, codigo, activo)
SELECT 'San Miguel', 'SAN-MIGUEL', 1
WHERE NOT EXISTS (SELECT 1 FROM zona_servicio WHERE codigo = 'SAN-MIGUEL');

INSERT INTO zona_servicio (nombre, codigo, activo)
SELECT 'Santiago de Surco', 'SURCO', 1
WHERE NOT EXISTS (SELECT 1 FROM zona_servicio WHERE codigo = 'SURCO');

INSERT INTO infraestructura_hidrica (id_zona, nombre, tipo, capacidad_litros, lat, lng, activo)
SELECT z.id_zona, 'Tanque Lima Centro', 'TANQUE', 50000.00, -12.0464000, -77.0428000, 1
FROM zona_servicio z
WHERE z.codigo = 'LIMA-CENTRO'
  AND NOT EXISTS (
    SELECT 1 FROM infraestructura_hidrica i WHERE i.nombre = 'Tanque Lima Centro'
  );

INSERT INTO infraestructura_hidrica (id_zona, nombre, tipo, capacidad_litros, lat, lng, activo)
SELECT z.id_zona, 'Reservorio San Miguel', 'RESERVORIO', 85000.00, -12.0763000, -77.0823000, 1
FROM zona_servicio z
WHERE z.codigo = 'SAN-MIGUEL'
  AND NOT EXISTS (
    SELECT 1 FROM infraestructura_hidrica i WHERE i.nombre = 'Reservorio San Miguel'
  );

INSERT INTO infraestructura_hidrica (id_zona, nombre, tipo, capacidad_litros, lat, lng, activo)
SELECT z.id_zona, 'Tanque Surco Norte', 'TANQUE', 65000.00, -12.1111000, -76.9919000, 1
FROM zona_servicio z
WHERE z.codigo = 'SURCO'
  AND NOT EXISTS (
    SELECT 1 FROM infraestructura_hidrica i WHERE i.nombre = 'Tanque Surco Norte'
  );

INSERT INTO lectura_iot (
  id_infraestructura,
  nivel_porcentaje,
  volumen_litros,
  bateria_porcentaje,
  senal_porcentaje,
  leido_en
)
SELECT i.id_infraestructura, 74.50, 37250.00, 92.00, 88.00, DATE_SUB(NOW(), INTERVAL 35 MINUTE)
FROM infraestructura_hidrica i
WHERE i.nombre = 'Tanque Lima Centro'
  AND NOT EXISTS (
    SELECT 1 FROM lectura_iot l WHERE l.id_infraestructura = i.id_infraestructura
  );

INSERT INTO lectura_iot (
  id_infraestructura,
  nivel_porcentaje,
  volumen_litros,
  bateria_porcentaje,
  senal_porcentaje,
  leido_en
)
SELECT i.id_infraestructura, 18.25, 15512.50, 76.00, 71.00, DATE_SUB(NOW(), INTERVAL 20 MINUTE)
FROM infraestructura_hidrica i
WHERE i.nombre = 'Reservorio San Miguel'
  AND NOT EXISTS (
    SELECT 1 FROM lectura_iot l WHERE l.id_infraestructura = i.id_infraestructura
  );

INSERT INTO lectura_iot (
  id_infraestructura,
  nivel_porcentaje,
  volumen_litros,
  bateria_porcentaje,
  senal_porcentaje,
  leido_en
)
SELECT i.id_infraestructura, 8.75, 5687.50, 64.00, 69.00, DATE_SUB(NOW(), INTERVAL 12 MINUTE)
FROM infraestructura_hidrica i
WHERE i.nombre = 'Tanque Surco Norte'
  AND NOT EXISTS (
    SELECT 1 FROM lectura_iot l WHERE l.id_infraestructura = i.id_infraestructura
  );

SET @mvp_seed_user_id = (
  SELECT id_usuario
  FROM usuario
  WHERE rol = 'ADMIN' AND estado = 'ACTIVO'
  ORDER BY id_usuario
  LIMIT 1
);

SET @mvp_seed_user_id = COALESCE(
  @mvp_seed_user_id,
  (
    SELECT id_usuario
    FROM usuario
    WHERE rol = 'OPERADOR' AND estado = 'ACTIVO'
    ORDER BY id_usuario
    LIMIT 1
  )
);

SET @mvp_seed_user_id = COALESCE(
  @mvp_seed_user_id,
  (
    SELECT id_usuario
    FROM usuario
    WHERE estado = 'ACTIVO'
    ORDER BY id_usuario
    LIMIT 1
  )
);

INSERT INTO alerta_servicio (
  id_zona,
  tipo,
  titulo,
  descripcion,
  severidad,
  estado,
  inicia_en,
  finaliza_en,
  creado_por
)
SELECT z.id_zona,
       'RIESGO_DESABASTECIMIENTO',
       'Riesgo de baja disponibilidad en San Miguel',
       'El reservorio principal registra nivel bajo. Se recomienda almacenar agua para usos esenciales mientras el equipo operativo estabiliza el servicio.',
       'ALTA',
       'ACTIVA',
       DATE_SUB(NOW(), INTERVAL 30 MINUTE),
       DATE_ADD(NOW(), INTERVAL 4 HOUR),
       @mvp_seed_user_id
FROM zona_servicio z
WHERE z.codigo = 'SAN-MIGUEL'
  AND @mvp_seed_user_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM alerta_servicio a
    WHERE a.titulo = 'Riesgo de baja disponibilidad en San Miguel'
  );

INSERT INTO alerta_servicio (
  id_zona,
  tipo,
  titulo,
  descripcion,
  severidad,
  estado,
  inicia_en,
  finaliza_en,
  creado_por
)
SELECT z.id_zona,
       'CORTE_PROGRAMADO',
       'Mantenimiento programado en Surco Norte',
       'Corte temporal por mantenimiento de valvulas. El servicio se restablecera progresivamente al finalizar la intervencion.',
       'MEDIA',
       'PROGRAMADA',
       DATE_ADD(NOW(), INTERVAL 2 HOUR),
       DATE_ADD(NOW(), INTERVAL 6 HOUR),
       @mvp_seed_user_id
FROM zona_servicio z
WHERE z.codigo = 'SURCO'
  AND @mvp_seed_user_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM alerta_servicio a
    WHERE a.titulo = 'Mantenimiento programado en Surco Norte'
  );

INSERT INTO alerta_iot (
  id_infraestructura,
  id_lectura,
  tipo,
  severidad,
  estado,
  mensaje
)
SELECT i.id_infraestructura,
       l.id_lectura,
       'NIVEL_BAJO',
       'ALTA',
       'ACTIVA',
       'Nivel bajo detectado en Reservorio San Miguel'
FROM infraestructura_hidrica i
JOIN lectura_iot l ON l.id_infraestructura = i.id_infraestructura
WHERE i.nombre = 'Reservorio San Miguel'
  AND l.id_lectura = (
    SELECT MAX(l2.id_lectura)
    FROM lectura_iot l2
    WHERE l2.id_infraestructura = i.id_infraestructura
  )
  AND NOT EXISTS (
    SELECT 1 FROM alerta_iot a
    WHERE a.id_infraestructura = i.id_infraestructura
      AND a.tipo = 'NIVEL_BAJO'
      AND a.estado = 'ACTIVA'
  );

INSERT INTO alerta_iot (
  id_infraestructura,
  id_lectura,
  tipo,
  severidad,
  estado,
  mensaje
)
SELECT i.id_infraestructura,
       l.id_lectura,
       'NIVEL_BAJO',
       'CRITICA',
       'ACTIVA',
       'Nivel critico detectado en Tanque Surco Norte'
FROM infraestructura_hidrica i
JOIN lectura_iot l ON l.id_infraestructura = i.id_infraestructura
WHERE i.nombre = 'Tanque Surco Norte'
  AND l.id_lectura = (
    SELECT MAX(l2.id_lectura)
    FROM lectura_iot l2
    WHERE l2.id_infraestructura = i.id_infraestructura
  )
  AND NOT EXISTS (
    SELECT 1 FROM alerta_iot a
    WHERE a.id_infraestructura = i.id_infraestructura
      AND a.tipo = 'NIVEL_BAJO'
      AND a.estado = 'ACTIVA'
  );
