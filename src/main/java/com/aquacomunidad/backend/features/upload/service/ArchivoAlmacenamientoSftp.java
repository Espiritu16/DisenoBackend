package com.aquacomunidad.backend.features.upload.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Service
@ConditionalOnProperty(name = "app.upload.storage", havingValue = "sftp")
public class ArchivoAlmacenamientoSftp implements ArchivoAlmacenamiento {

  private final String host;
  private final int port;
  private final String user;
  private final String privateKey;
  private final String privateKeyPath;
  private final String remoteBaseDir;
  private final String strictHostKeyChecking;

  public ArchivoAlmacenamientoSftp(
      @Value("${app.upload.sftp.host:${UPLOAD_SFTP_HOST:}}") String host,
      @Value("${app.upload.sftp.port:${UPLOAD_SFTP_PORT:22}}") int port,
      @Value("${app.upload.sftp.user:${UPLOAD_SFTP_USER:}}") String user,
      @Value("${app.upload.sftp.private-key:${UPLOAD_SFTP_PRIVATE_KEY:}}") String privateKey,
      @Value("${app.upload.sftp.private-key-path:${UPLOAD_SFTP_PRIVATE_KEY_PATH:}}") String privateKeyPath,
      @Value("${app.upload.sftp.remote-base-dir:${UPLOAD_REMOTE_BASE_DIR:/opt/proyectos/aquacomunidad/uploads}}") String remoteBaseDir,
      @Value("${app.upload.sftp.strict-host-key-checking:${UPLOAD_SFTP_STRICT_HOST_KEY_CHECKING:no}}") String strictHostKeyChecking) {
    this.host = host;
    this.port = port;
    this.user = user;
    this.privateKey = privateKey;
    this.privateKeyPath = privateKeyPath;
    this.remoteBaseDir = limpiarDirectorioBase(remoteBaseDir);
    this.strictHostKeyChecking = strictHostKeyChecking == null || strictHostKeyChecking.isBlank()
        ? "no"
        : strictHostKeyChecking;
  }

  @Override
  public void guardar(Path archivoLocal, String rutaRelativa) throws Exception {
    validarConfiguracion();
    String remotePath = remoteBaseDir + "/" + limpiarRutaRelativa(rutaRelativa);

    JSch jsch = new JSch();
    if (privateKeyPath != null && !privateKeyPath.isBlank()) {
      jsch.addIdentity(privateKeyPath);
    } else {
      jsch.addIdentity("aquacomunidad-upload", normalizarPrivateKey().getBytes(StandardCharsets.UTF_8), null, null);
    }

    Session session = jsch.getSession(user, host, port);
    session.setConfig("StrictHostKeyChecking", strictHostKeyChecking);
    session.connect(15_000);
    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
    try {
      channel.connect(15_000);
      crearDirectorios(channel, remotePath.substring(0, remotePath.lastIndexOf('/')));
      channel.put(archivoLocal.toString(), remotePath, ChannelSftp.OVERWRITE);
    } finally {
      if (channel.isConnected()) {
        channel.disconnect();
      }
      if (session.isConnected()) {
        session.disconnect();
      }
    }
  }

  private void validarConfiguracion() {
    if (host == null || host.isBlank()) {
      throw new IllegalStateException("UPLOAD_SFTP_HOST es obligatorio");
    }
    if (user == null || user.isBlank()) {
      throw new IllegalStateException("UPLOAD_SFTP_USER es obligatorio");
    }
    if ((privateKey == null || privateKey.isBlank()) && (privateKeyPath == null || privateKeyPath.isBlank())) {
      throw new IllegalStateException("UPLOAD_SFTP_PRIVATE_KEY o UPLOAD_SFTP_PRIVATE_KEY_PATH es obligatorio");
    }
  }

  private String limpiarRutaRelativa(String rutaRelativa) {
    String limpia = rutaRelativa == null ? "" : rutaRelativa.replace('\\', '/');
    if (limpia.startsWith("/") || limpia.contains("../") || limpia.contains("..")) {
      throw new IllegalArgumentException("Ruta de archivo invalida");
    }
    return limpia;
  }

  private String limpiarDirectorioBase(String value) {
    String limpio = value == null || value.isBlank() ? "/opt/proyectos/aquacomunidad/uploads" : value.trim();
    while (limpio.endsWith("/")) {
      limpio = limpio.substring(0, limpio.length() - 1);
    }
    return limpio;
  }

  private String normalizarPrivateKey() {
    return privateKey.replace("\\n", "\n").trim() + "\n";
  }

  private void crearDirectorios(ChannelSftp channel, String path) throws SftpException {
    StringBuilder actual = new StringBuilder();
    for (String parte : path.split("/")) {
      if (parte.isBlank()) {
        continue;
      }
      actual.append('/').append(parte);
      String directorio = actual.toString();
      try {
        channel.cd(directorio);
      } catch (SftpException ex) {
        channel.mkdir(directorio);
      }
    }
  }
}
