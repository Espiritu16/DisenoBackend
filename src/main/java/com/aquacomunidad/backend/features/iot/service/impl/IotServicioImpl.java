package com.aquacomunidad.backend.features.iot.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquacomunidad.backend.common.enums.EstadoAlertaIot;
import com.aquacomunidad.backend.common.enums.SeveridadAlertaIot;
import com.aquacomunidad.backend.common.enums.TipoAlertaIot;
import com.aquacomunidad.backend.exception.ExcepcionApi;
import com.aquacomunidad.backend.features.iot.dto.LecturaIotSolicitudDto;
import com.aquacomunidad.backend.features.iot.dto.NivelAguaDto;
import com.aquacomunidad.backend.features.iot.entity.AlertaIotEntidad;
import com.aquacomunidad.backend.features.iot.entity.InfraestructuraHidricaEntidad;
import com.aquacomunidad.backend.features.iot.entity.LecturaIotEntidad;
import com.aquacomunidad.backend.features.iot.repository.AlertaIotRepositorio;
import com.aquacomunidad.backend.features.iot.repository.InfraestructuraHidricaRepositorio;
import com.aquacomunidad.backend.features.iot.repository.LecturaIotRepositorio;
import com.aquacomunidad.backend.features.iot.service.IotServicio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IotServicioImpl implements IotServicio {

  private static final BigDecimal UMBRAL_CRITICO = new BigDecimal("10.00");
  private static final BigDecimal UMBRAL_BAJO = new BigDecimal("20.00");

  private final InfraestructuraHidricaRepositorio infraestructuraRepositorio;
  private final LecturaIotRepositorio lecturaRepositorio;
  private final AlertaIotRepositorio alertaIotRepositorio;

  @Override
  @Transactional(readOnly = true)
  public List<NivelAguaDto> listarNiveles() {
    return infraestructuraRepositorio.findByActivoTrue().stream()
        .map(this::nivelActual)
        .toList();
  }

  @Override
  @Transactional
  public NivelAguaDto registrarLectura(LecturaIotSolicitudDto request) {
    InfraestructuraHidricaEntidad infraestructura = infraestructuraRepositorio.findById(request.getInfraestructuraId())
        .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Infraestructura hidrica no encontrada"));

    LecturaIotEntidad lectura = new LecturaIotEntidad();
    lectura.setInfraestructura(infraestructura);
    lectura.setNivelPorcentaje(request.getNivelPorcentaje());
    lectura.setVolumenLitros(request.getVolumenLitros());
    lectura.setBateriaPorcentaje(request.getBateriaPorcentaje());
    lectura.setSenalPorcentaje(request.getSenalPorcentaje());
    lectura.setLeidoEn(request.getLeidoEn());
    LecturaIotEntidad guardada = lecturaRepositorio.save(lectura);
    crearAlertaSiCorresponde(infraestructura, guardada);
    return aNivel(infraestructura, guardada);
  }

  private NivelAguaDto nivelActual(InfraestructuraHidricaEntidad infraestructura) {
    return lecturaRepositorio.findTopByInfraestructuraIdOrderByLeidoEnDesc(infraestructura.getId())
        .map(lectura -> aNivel(infraestructura, lectura))
        .orElseGet(() -> NivelAguaDto.builder()
            .infraestructuraId(infraestructura.getId())
            .nombre(infraestructura.getNombre())
            .zona(infraestructura.getZona().getNombre())
            .tipo(infraestructura.getTipo())
            .estado("SIN_DATOS")
            .build());
  }

  private NivelAguaDto aNivel(InfraestructuraHidricaEntidad infraestructura, LecturaIotEntidad lectura) {
    return NivelAguaDto.builder()
        .infraestructuraId(infraestructura.getId())
        .nombre(infraestructura.getNombre())
        .zona(infraestructura.getZona().getNombre())
        .tipo(infraestructura.getTipo())
        .nivelPorcentaje(lectura.getNivelPorcentaje())
        .bateriaPorcentaje(lectura.getBateriaPorcentaje())
        .senalPorcentaje(lectura.getSenalPorcentaje())
        .estado(estadoNivel(lectura.getNivelPorcentaje()))
        .actualizadoEn(lectura.getLeidoEn())
        .build();
  }

  private String estadoNivel(BigDecimal nivel) {
    if (nivel == null) {
      return "SIN_DATOS";
    }
    if (nivel.compareTo(UMBRAL_CRITICO) <= 0) {
      return "CRITICO";
    }
    if (nivel.compareTo(UMBRAL_BAJO) <= 0) {
      return "BAJO";
    }
    return "NORMAL";
  }

  private void crearAlertaSiCorresponde(InfraestructuraHidricaEntidad infraestructura, LecturaIotEntidad lectura) {
    BigDecimal nivel = lectura.getNivelPorcentaje();
    if (nivel == null || nivel.compareTo(UMBRAL_BAJO) > 0) {
      return;
    }

    AlertaIotEntidad alerta = new AlertaIotEntidad();
    alerta.setInfraestructura(infraestructura);
    alerta.setLectura(lectura);
    alerta.setTipo(TipoAlertaIot.NIVEL_BAJO);
    alerta.setSeveridad(nivel.compareTo(UMBRAL_CRITICO) <= 0 ? SeveridadAlertaIot.CRITICA : SeveridadAlertaIot.ALTA);
    alerta.setEstado(EstadoAlertaIot.ACTIVA);
    alerta.setMensaje("Nivel critico en " + infraestructura.getNombre());
    alertaIotRepositorio.save(alerta);
  }
}
