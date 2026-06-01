# Auditoria backend AquaComunidad

Fecha: 2026-05-31

## Resumen ejecutivo

El backend esta bien separado por capas y la suite actual pasa (`./mvnw test`: 34 tests OK), pero hay riesgos claros de rendimiento cuando crezcan reportes, casos, imagenes, evidencias y conversaciones. Los problemas principales son consultas no paginadas, N+1 por relaciones `LAZY` mapeadas dentro de listas, conteos calculados en memoria y endpoints de dashboard/chatbot/upload que pueden bloquear threads por trabajo costoso.

Prioridad recomendada:

1. Corregir N+1 y paginacion en listados de reportes y casos.
2. Pasar dashboard/resumenes a consultas agregadas SQL/JPA (`group by`, `count`, `limit`) en vez de cargar filas completas.
3. Endurecer autorizacion de uploads, rate limiting de auth/chatbot y validacion real de imagenes.
4. Desactivar `open-in-view` y agregar tests de conteo de queries para evitar regresiones.

## Hallazgos de rendimiento

### P1 - N+1 en listados de reportes

Evidencia:

- `ReporteServicioImpl.listarTodos/listarPorUsuario/listarPorEstado/listarConFiltros/listarMisReportes` devuelve listas completas y mapea entidad por entidad: `src/main/java/com/aquacomunidad/backend/features/reporte/service/impl/ReporteServicioImpl.java:94`, `:100`, `:106`, `:121`, `:129`.
- `ReporteMapeador.aRespuesta` accede `entity.getUsuario().getId()`, `entity.getTipo().getNombre()` y `entity.getImagenes().stream()`: `src/main/java/com/aquacomunidad/backend/features/reporte/mapper/ReporteMapeador.java:32`, `:33`, `:36`.
- `ReporteEntidad` declara `usuario`, `tipo` e `imagenes` como relaciones lazy: `src/main/java/com/aquacomunidad/backend/features/reporte/entity/ReporteEntidad.java:39`, `:43`, `:78`.

Impacto:

Un listado de N reportes puede ejecutar 1 consulta base + consultas por usuario/tipo/imagenes. En MySQL real, esto escala mal y puede volver lentos endpoints que deberian responder en decenas de ms.

Remediacion:

- Cambiar listados a `Page<ReporteRespuestaDto>` o proyecciones DTO.
- Para respuestas completas, usar `@EntityGraph(attributePaths = {"usuario", "tipo", "imagenes"})` o JPQL `left join fetch r.usuario left join fetch r.tipo left join fetch r.imagenes`.
- Para listados administrativos, preferir una proyeccion resumida que no cargue imagenes si no se necesitan.
- Agregar tests/instrumentacion de conteo de queries para `GET /api/v1/reportes` y `GET /api/v1/reportes/mis-reportes`.

### P1 - Listados de casos con N+1 y filtro en memoria

Evidencia:

- `CasoServicioImpl.listarTodos` usa `findAll()` sin limite para administradores: `src/main/java/com/aquacomunidad/backend/features/caso/service/impl/CasoServicioImpl.java:189`.
- Para operadores con filtro por estado, primero trae todos sus casos y filtra en Java: `src/main/java/com/aquacomunidad/backend/features/caso/service/impl/CasoServicioImpl.java:207`.
- `CasoMapeador` accede `reporteOrigen`, `responsable` y `evidencias`: `src/main/java/com/aquacomunidad/backend/features/caso/mapper/CasoMapeador.java:15`, `:16`, `:21`.
- Esas relaciones son lazy en `CasoEntidad`: `src/main/java/com/aquacomunidad/backend/features/caso/entity/CasoEntidad.java:39`, `:43`, `:71`.

Impacto:

Un operador con muchos casos pagara costo de todos los casos asignados aunque pida un estado. El mapeo puede generar N+1 sobre evidencias y relaciones.

Remediacion:

- Agregar `findByResponsableIdAndEstado(Long responsableId, EstadoCaso estado, Pageable pageable)`.
- Agregar `Page<CasoEntidad>` / proyecciones DTO.
- Usar entity graph/fetch join con `reporteOrigen`, `responsable` y `evidencias` solo cuando el response los requiera.

### P1 - Dashboard carga todos los reportes para top zonas

Evidencia:

- `reportesPorZona()` hace `reporteRepositorio.findAll()` y agrupa en memoria: `src/main/java/com/aquacomunidad/backend/features/tablero/service/impl/TableroServicioImpl.java:71`.
- `actividadSemanal()` carga filas completas del rango y agrupa en Java: `src/main/java/com/aquacomunidad/backend/features/tablero/service/impl/TableroServicioImpl.java:55`.
- `getKpis()` dispara varios `countByEstado` separados: `src/main/java/com/aquacomunidad/backend/features/tablero/service/impl/TableroServicioImpl.java:39`.

Impacto:

El dashboard es un endpoint que normalmente se consulta con frecuencia. Con miles de reportes, `findAll()` para top zonas sera uno de los cuellos de botella mas visibles.

Remediacion:

- Crear consultas agregadas:
  - `select zona, count(*) from reporte group by zona order by count(*) desc limit 5`
  - conteo diario con `date(fecha_creacion)` y rango.
  - conteos por estado con un solo `group by estado`.
- Evaluar cache corto de 30-120 segundos para KPIs si el dashboard se refresca seguido.

### P1 - Resumenes de reportes cargan todo el historial del usuario

Evidencia:

- `obtenerResumenMisReportes` carga `findByUsuarioId(usuarioId)` y cuenta estados en memoria: `src/main/java/com/aquacomunidad/backend/features/reporte/service/impl/ReporteServicioImpl.java:136`, `:140`.
- `obtenerUltimoReporte` vuelve a cargar todos los reportes y calcula `max` en Java: `src/main/java/com/aquacomunidad/backend/features/reporte/service/impl/ReporteServicioImpl.java:154`.
- El chatbot llama `reporteServicio.listarMisReportes(usuarioId)` y despues filtra/ordena/cuenta: `src/main/java/com/aquacomunidad/backend/features/chatbot/service/impl/ChatbotServicioImpl.java:383`, `:403`, `:429`, `:470`, `:494`.

Impacto:

Usuarios con muchos reportes hacen que endpoints simples como "ultimo reporte" o "cuantos tengo" carguen objetos completos e imagenes, amplificando N+1.

Remediacion:

- Crear metodos especificos:
  - `countByUsuarioIdAndEstado(...)`
  - `findTop1ByUsuarioIdOrderByFechaCreacionDesc(...)`
  - `findTop5ByUsuarioIdOrderByFechaCreacionDesc(...)`
  - `findByIdAndUsuarioId(...)`
- En chatbot, usar esos metodos en vez de traer todos los reportes.

### P2 - Open-in-view esta activo

Evidencia:

- `./mvnw test` muestra warning: `spring.jpa.open-in-view is enabled by default`.
- No existe `spring.jpa.open-in-view=false` en `src/main/resources/application.properties`.

Impacto:

Permite que relaciones lazy se resuelvan fuera del servicio y oculta N+1. Esto dificulta detectar consultas inesperadas y hace menos predecible el costo de cada endpoint.

Remediacion:

- Agregar `spring.jpa.open-in-view=false`.
- Antes de hacerlo, corregir fetch joins/proyecciones en endpoints que hoy dependen de lazy loading dentro de la transaccion o cerca del response.

### P2 - Busquedas con `lower(... like '%texto%')` no aprovechan indices normales

Evidencia:

- `buscarConFiltros` usa `lower(r.tipo.nombre) like lower(concat('%', :tipo, '%'))`, `lower(r.tipo.codigo)` y `lower(r.zona) like '%...'`: `src/main/java/com/aquacomunidad/backend/features/reporte/repository/ReporteRepositorio.java:29`, `:31`.
- El esquema tiene indices simples en `zona`, `estado`, `id_tipo`, `fecha_creacion`: `src/main/resources/db/db_esquema.sql:219`, `:220`, `:221`, `:222`, `:223`.

Impacto:

Los filtros por texto pueden caer en scans grandes. Con pocos datos no se nota; con datos reales puede ser lento.

Remediacion:

- Para `tipo`, filtrar por `id_tipo` o `codigo` exacto normalizado.
- Para `zona`, usar exact match normalizado o columna generada/indice funcional si se necesita case-insensitive.
- Para texto libre, considerar FULLTEXT solo si el producto realmente necesita busqueda parcial.

## Hallazgos de seguridad y robustez

### P1 - Uploads no estan restringidos por rol especifico

Evidencia:

- La configuracion de seguridad no define reglas para `/api/v1/uploads/**`; cae en `.anyRequest().authenticated()`: `src/main/java/com/aquacomunidad/backend/config/ConfiguracionSeguridad.java:56`, `:66`.
- Existen endpoints separados para reportes y casos: `src/main/java/com/aquacomunidad/backend/features/upload/controller/ArchivoControlador.java:26`, `:37`.

Impacto:

Cualquier usuario autenticado podria subir evidencia en carpeta de casos o reportes. Aunque no necesariamente pueda asociarla despues, consume almacenamiento/proceso y debilita el modelo de autorizacion.

Remediacion:

- `POST /api/v1/uploads/reportes` -> `CIUDADANO`.
- `POST /api/v1/uploads/casos` -> `ADMIN` u `OPERADOR`.
- Agregar tests de seguridad para 401/403 por rol.

### P1 - Recuperacion de contrasena permite enumerar correos y no tiene rate limit

Evidencia:

- Si el correo no existe se devuelve `NOT_FOUND`: `src/main/java/com/aquacomunidad/backend/features/autenticacion/service/impl/AutenticacionServicioImpl.java:194`.
- Cada solicitud invalida tokens, genera codigo y envia email sin throttling visible: `src/main/java/com/aquacomunidad/backend/features/autenticacion/service/impl/AutenticacionServicioImpl.java:197`, `:199`, `:210`.

Impacto:

Un atacante puede descubrir cuentas registradas y forzar envio repetido de correos/codigos.

Remediacion:

- Responder siempre con mensaje generico: "si el correo existe, enviaremos un codigo".
- Agregar rate limit por IP + correo y cooldown por usuario.
- Registrar intentos sin exponer existencia.

### P1 - Validacion de imagen confia en `Content-Type` y el proceso WebP no tiene timeout

Evidencia:

- `ArchivoServicio.validar` solo revisa `file.getContentType()`: `src/main/java/com/aquacomunidad/backend/features/upload/service/ArchivoServicio.java:144`.
- `CwebpConverter` llama proceso externo y espera indefinidamente con `process.waitFor()`: `src/main/java/com/aquacomunidad/backend/features/upload/service/ArchivoServicio.java:198`, `:200`.

Impacto:

Archivos no imagen pueden pasar con MIME falso. Un proceso `cwebp` colgado puede bloquear threads de request y degradar el backend.

Remediacion:

- Validar magic bytes/dimensiones con `ImageIO` o libreria especializada antes de convertir.
- Ejecutar conversion con timeout y destruir el proceso si excede.
- Considerar cola/background job para conversiones si el trafico aumenta.

### P2 - Secreto JWT por defecto inseguro para produccion

Evidencia:

- `application.properties` tiene default: `CAMBIAR_ESTE_SECRETO...`: `src/main/resources/application.properties:25`.
- `ServicioJwt` lo usa directamente para firmar tokens: `src/main/java/com/aquacomunidad/backend/security/ServicioJwt.java:30`, `:33`.

Impacto:

Si produccion arranca sin `APP_JWT_SECRETO`, los tokens quedan firmados con un secreto conocido por el repositorio.

Remediacion:

- Fallar arranque si `APP_JWT_SECRETO` no esta definido en perfiles no locales.
- Validar longitud y entropia minima.

### P2 - SFTP desactiva host key checking por defecto

Evidencia:

- Default `UPLOAD_SFTP_STRICT_HOST_KEY_CHECKING:no`: `src/main/resources/application.properties:45`.
- Se aplica directamente a JSch: `src/main/java/com/aquacomunidad/backend/features/upload/service/ArchivoAlmacenamientoSftp.java:58`.

Impacto:

En produccion permite MITM contra la conexion SFTP.

Remediacion:

- Default `yes` en produccion.
- Configurar `known_hosts` o pin de host key.

### P2 - Swagger/OpenAPI expuesto publicamente

Evidencia:

- `/api-docs/**`, `/swagger-ui.html`, `/swagger-ui/**` estan en `permitAll`: `src/main/java/com/aquacomunidad/backend/config/ConfiguracionSeguridad.java:57`.

Impacto:

Expone superficie de API y contratos en produccion. No es una vulnerabilidad por si sola, pero facilita enumeracion.

Remediacion:

- Mantener publico solo en dev/staging.
- En produccion, proteger por rol admin o deshabilitar `springdoc`.

## Hallazgos de datos y arquitectura

### P2 - No hay migraciones versionadas

Evidencia:

- Solo existe dump SQL en `src/main/resources/db/db_esquema.sql`; no se observa Flyway/Liquibase en `pom.xml`.

Impacto:

El esquema puede derivar entre ambientes. Esto aumenta riesgo al agregar indices para optimizacion.

Remediacion:

- Introducir Flyway o Liquibase.
- Convertir el esquema actual a migracion base y los indices nuevos a migraciones incrementales.

### P2 - Indices mejorables para consultas frecuentes

Evidencia:

- El esquema tiene indices simples, pero las consultas frecuentes combinan usuario/estado/fecha, responsable/estado y zona/fecha: `src/main/resources/db/db_esquema.sql:219-224`, `:40-42`.

Remediacion sugerida:

- `reporte(id_usuario, fecha_creacion desc)`
- `reporte(id_usuario, estado, fecha_creacion desc)`
- `reporte(estado, fecha_creacion desc)`
- `caso_operativo(id_responsable, estado, fecha_asignacion desc)`
- Revisar `chatbot_conversacion(id_usuario, fecha_conversacion)` ya esta cubierto por unique.

## Plan de correccion sugerido

1. Cambiar contratos de listados a paginados (`Page` o wrapper propio con `items`, `page`, `size`, `total`).
2. Crear repositorios con fetch join/entity graph/proyecciones para reportes y casos.
3. Reescribir dashboard y resumenes con agregaciones en base de datos.
4. Optimizar chatbot para consultar solo top N, ultimo o conteos, nunca todos los reportes.
5. Endurecer upload/auth: roles por endpoint, validacion real de imagen, timeout de `cwebp`, rate limiting.
6. Agregar `spring.jpa.open-in-view=false` cuando las consultas necesarias esten corregidas.
7. Agregar migraciones versionadas con indices compuestos.
8. Agregar tests de rendimiento logico: conteo de queries para listados y tests de autorizacion por rol.

## Validacion realizada

- Comando: `./mvnw test`
- Resultado: BUILD SUCCESS, 34 tests, 0 failures, 0 errors.

Riesgo residual: no se ejecuto profiling con datos grandes ni `EXPLAIN ANALYZE` en MySQL real. Las conclusiones se basan en flujo de codigo, entidades JPA, repositorios y esquema SQL.
