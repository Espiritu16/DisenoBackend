-- MySQL dump 10.13  Distrib 9.5.0, for macos26.1 (arm64)
--
-- Host: localhost    Database: aquacomunidad_db
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `caso_operativo`
--

DROP TABLE IF EXISTS `caso_operativo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caso_operativo` (
  `id_caso` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_reporte_origen` bigint unsigned NOT NULL,
  `id_responsable` bigint unsigned NOT NULL,
  `prioridad` enum('BAJA','MEDIA','ALTA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEDIA',
  `estado` enum('EN_PROCESO','RESUELTO','ESCALADO','RECHAZADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EN_PROCESO',
  `observaciones` varchar(1500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `evidencia_cierre` varchar(600) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_asignacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_cierre` datetime DEFAULT NULL,
  `creado_por` bigint unsigned NOT NULL,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_caso`),
  UNIQUE KEY `uq_caso_reporte` (`id_reporte_origen`),
  KEY `fk_caso_creado_por` (`creado_por`),
  KEY `idx_caso_estado` (`estado`),
  KEY `idx_caso_responsable` (`id_responsable`),
  KEY `idx_caso_prioridad` (`prioridad`),
  CONSTRAINT `fk_caso_creado_por` FOREIGN KEY (`creado_por`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_caso_reporte` FOREIGN KEY (`id_reporte_origen`) REFERENCES `reporte` (`id_reporte`),
  CONSTRAINT `fk_caso_responsable` FOREIGN KEY (`id_responsable`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `chk_caso_cierre_evidencia` CHECK (((`estado` <> _utf8mb4'RESUELTO') or ((`estado` = _utf8mb4'RESUELTO') and (`evidencia_cierre` is not null) and (char_length(trim(`evidencia_cierre`)) > 0))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `caso_evidencia`
--

DROP TABLE IF EXISTS `caso_evidencia`;
CREATE TABLE `caso_evidencia` (
  `id_caso_evidencia` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_caso` bigint unsigned NOT NULL,
  `url` varchar(800) COLLATE utf8mb4_unicode_ci NOT NULL,
  `public_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `orden` int NOT NULL DEFAULT 0,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_caso_evidencia`),
  KEY `idx_caso_evidencia_caso` (`id_caso`,`orden`),
  CONSTRAINT `fk_caso_evidencia_caso` FOREIGN KEY (`id_caso`) REFERENCES `caso_operativo` (`id_caso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `catalogo_tipo_incidencia`
--

DROP TABLE IF EXISTS `catalogo_tipo_incidencia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `catalogo_tipo_incidencia` (
  `id_tipo` smallint unsigned NOT NULL AUTO_INCREMENT,
  `codigo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_tipo`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chatbot_conversacion`
--

DROP TABLE IF EXISTS `chatbot_conversacion`;
CREATE TABLE `chatbot_conversacion` (
  `id_conversacion` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint unsigned NOT NULL,
  `fecha_conversacion` date NOT NULL,
  `titulo` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_conversacion`),
  UNIQUE KEY `uq_chatbot_conversacion_usuario_fecha` (`id_usuario`,`fecha_conversacion`),
  KEY `idx_chatbot_conversacion_usuario_actualizado` (`id_usuario`,`actualizado_en`),
  CONSTRAINT `fk_chatbot_conversacion_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `chatbot_mensaje`
--

DROP TABLE IF EXISTS `chatbot_mensaje`;
CREATE TABLE `chatbot_mensaje` (
  `id_mensaje` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_conversacion` bigint unsigned NOT NULL,
  `rol` enum('USUARIO','ASISTENTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `contenido` varchar(2500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `proveedor` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `modelo` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ia_disponible` tinyint(1) NOT NULL DEFAULT '0',
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_mensaje`),
  KEY `idx_chatbot_mensaje_conversacion_fecha` (`id_conversacion`,`creado_en`),
  CONSTRAINT `fk_chatbot_mensaje_conversacion` FOREIGN KEY (`id_conversacion`) REFERENCES `chatbot_conversacion` (`id_conversacion`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `historial_estado_caso`
--

DROP TABLE IF EXISTS `historial_estado_caso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_estado_caso` (
  `id_historial` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_caso` bigint unsigned NOT NULL,
  `estado_anterior` enum('EN_PROCESO','RESUELTO','ESCALADO','RECHAZADO') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado_nuevo` enum('EN_PROCESO','RESUELTO','ESCALADO','RECHAZADO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `observacion` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cambiado_por` bigint unsigned NOT NULL,
  `fecha_cambio` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_historial`),
  KEY `fk_hist_caso_usuario` (`cambiado_por`),
  KEY `idx_hist_caso_fecha` (`id_caso`,`fecha_cambio`),
  CONSTRAINT `fk_hist_caso` FOREIGN KEY (`id_caso`) REFERENCES `caso_operativo` (`id_caso`),
  CONSTRAINT `fk_hist_caso_usuario` FOREIGN KEY (`cambiado_por`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `historial_estado_reporte`
--

DROP TABLE IF EXISTS `historial_estado_reporte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_estado_reporte` (
  `id_historial` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_reporte` bigint unsigned NOT NULL,
  `estado_anterior` enum('PENDIENTE','EN_PROCESO','RESUELTO','DUPLICADO','RECHAZADO','ESCALADO') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado_nuevo` enum('PENDIENTE','EN_PROCESO','RESUELTO','DUPLICADO','RECHAZADO','ESCALADO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `observacion` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cambiado_por` bigint unsigned NOT NULL,
  `fecha_cambio` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_historial`),
  KEY `fk_hist_reporte_usuario` (`cambiado_por`),
  KEY `idx_hist_reporte_fecha` (`id_reporte`,`fecha_cambio`),
  CONSTRAINT `fk_hist_reporte` FOREIGN KEY (`id_reporte`) REFERENCES `reporte` (`id_reporte`),
  CONSTRAINT `fk_hist_reporte_usuario` FOREIGN KEY (`cambiado_por`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refresh_token`
--

DROP TABLE IF EXISTS `refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_token` (
  `id_refresh_token` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint unsigned NOT NULL,
  `token_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `hash_token` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expira_en` datetime NOT NULL,
  `estado` enum('ACTIVO','ROTADO','REVOCADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVO',
  `reemplazado_por_token_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `revocado_en` datetime DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_refresh_token`),
  UNIQUE KEY `uq_refresh_token_token_id` (`token_id`),
  KEY `idx_refresh_token_session` (`session_id`),
  KEY `idx_refresh_token_usuario_estado` (`id_usuario`,`estado`),
  CONSTRAINT `fk_refresh_token_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reporte`
--

DROP TABLE IF EXISTS `reporte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reporte` (
  `id_reporte` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint unsigned NOT NULL,
  `id_tipo` smallint unsigned NOT NULL,
  `descripcion` varchar(1200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `foto_url` varchar(600) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lat` decimal(10,7) NOT NULL,
  `lng` decimal(10,7) NOT NULL,
  `direccion` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `zona` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `estado` enum('PENDIENTE','EN_PROCESO','RESUELTO','DUPLICADO','RECHAZADO','ESCALADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDIENTE',
  `posible_duplicado` tinyint(1) NOT NULL DEFAULT '0',
  `duplicado_de_id` bigint unsigned DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_reporte`),
  KEY `fk_reporte_duplicado_de` (`duplicado_de_id`),
  KEY `idx_reporte_usuario` (`id_usuario`),
  KEY `idx_reporte_estado` (`estado`),
  KEY `idx_reporte_tipo` (`id_tipo`),
  KEY `idx_reporte_zona` (`zona`),
  KEY `idx_reporte_fecha_creacion` (`fecha_creacion`),
  KEY `idx_reporte_duplicado` (`posible_duplicado`,`id_tipo`,`zona`,`fecha_creacion`),
  CONSTRAINT `fk_reporte_duplicado_de` FOREIGN KEY (`duplicado_de_id`) REFERENCES `reporte` (`id_reporte`),
  CONSTRAINT `fk_reporte_tipo` FOREIGN KEY (`id_tipo`) REFERENCES `catalogo_tipo_incidencia` (`id_tipo`),
  CONSTRAINT `fk_reporte_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `chk_reporte_desc_no_vacia` CHECK ((char_length(trim(`descripcion`)) > 0)),
  CONSTRAINT `chk_reporte_direccion_no_vacia` CHECK ((char_length(trim(`direccion`)) > 0)),
  CONSTRAINT `chk_reporte_foto_no_vacia` CHECK ((char_length(trim(`foto_url`)) > 0)),
  CONSTRAINT `chk_reporte_lat` CHECK (((`lat` >= -(90)) and (`lat` <= 90))),
  CONSTRAINT `chk_reporte_lng` CHECK (((`lng` >= -(180)) and (`lng` <= 180))),
  CONSTRAINT `chk_reporte_zona_no_vacia` CHECK ((char_length(trim(`zona`)) > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reporte_imagen`
--

DROP TABLE IF EXISTS `reporte_imagen`;
CREATE TABLE `reporte_imagen` (
  `id_reporte_imagen` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_reporte` bigint unsigned NOT NULL,
  `url` varchar(800) COLLATE utf8mb4_unicode_ci NOT NULL,
  `public_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `orden` int NOT NULL DEFAULT 0,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_reporte_imagen`),
  KEY `idx_reporte_imagen_reporte` (`id_reporte`,`orden`),
  CONSTRAINT `fk_reporte_imagen_reporte` FOREIGN KEY (`id_reporte`) REFERENCES `reporte` (`id_reporte`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `token_recuperacion_contrasena`
--

DROP TABLE IF EXISTS `token_recuperacion_contrasena`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token_recuperacion_contrasena` (
  `id_token` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint unsigned NOT NULL,
  `hash_codigo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `hash_token_restablecimiento` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `expira_en` datetime NOT NULL,
  `expira_token_restablecimiento_en` datetime DEFAULT NULL,
  `usado_en` datetime DEFAULT NULL,
  `estado` enum('PENDIENTE','CODIGO_CONFIRMADO','USADO','EXPIRADO','CANCELADO') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDIENTE',
  `intentos` tinyint unsigned NOT NULL DEFAULT '0',
  `max_intentos` tinyint unsigned NOT NULL DEFAULT '5',
  `codigo_confirmado_en` datetime DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_token`),
  KEY `idx_token_usuario_estado` (`id_usuario`,`estado`),
  KEY `idx_token_expira` (`expira_en`),
  CONSTRAINT `fk_token_recuperacion_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `chk_token_intentos` CHECK ((`intentos` <= `max_intentos`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `correo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rol` enum('CIUDADANO','ADMIN','OPERADOR') COLLATE utf8mb4_unicode_ci NOT NULL,
  `estado` enum('ACTIVO','INACTIVO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVO',
  `telefono` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uq_usuario_correo` (`correo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `vw_kpi_reporte_por_estado`
--

DROP TABLE IF EXISTS `vw_kpi_reporte_por_estado`;
/*!50001 DROP VIEW IF EXISTS `vw_kpi_reporte_por_estado`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_kpi_reporte_por_estado` AS SELECT 
 1 AS `estado`,
 1 AS `total`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_kpi_reporte_por_zona`
--

DROP TABLE IF EXISTS `vw_kpi_reporte_por_zona`;
/*!50001 DROP VIEW IF EXISTS `vw_kpi_reporte_por_zona`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_kpi_reporte_por_zona` AS SELECT 
 1 AS `zona`,
 1 AS `total`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_kpi_tiempo_resolucion_horas`
--

DROP TABLE IF EXISTS `vw_kpi_tiempo_resolucion_horas`;
/*!50001 DROP VIEW IF EXISTS `vw_kpi_tiempo_resolucion_horas`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_kpi_tiempo_resolucion_horas` AS SELECT 
 1 AS `id_caso`,
 1 AS `id_reporte_origen`,
 1 AS `horas_resolucion`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `vw_kpi_reporte_por_estado`
--

/*!50001 DROP VIEW IF EXISTS `vw_kpi_reporte_por_estado`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_kpi_reporte_por_estado` AS select `reporte`.`estado` AS `estado`,count(0) AS `total` from `reporte` group by `reporte`.`estado` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_kpi_reporte_por_zona`
--

/*!50001 DROP VIEW IF EXISTS `vw_kpi_reporte_por_zona`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_kpi_reporte_por_zona` AS select `reporte`.`zona` AS `zona`,count(0) AS `total` from `reporte` group by `reporte`.`zona` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_kpi_tiempo_resolucion_horas`
--

/*!50001 DROP VIEW IF EXISTS `vw_kpi_tiempo_resolucion_horas`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_kpi_tiempo_resolucion_horas` AS select `c`.`id_caso` AS `id_caso`,`c`.`id_reporte_origen` AS `id_reporte_origen`,timestampdiff(HOUR,`c`.`fecha_asignacion`,`c`.`fecha_cierre`) AS `horas_resolucion` from `caso_operativo` `c` where ((`c`.`estado` = 'RESUELTO') and (`c`.`fecha_cierre` is not null)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-22 16:45:02
