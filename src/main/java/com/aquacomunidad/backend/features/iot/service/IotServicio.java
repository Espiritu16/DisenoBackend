package com.aquacomunidad.backend.features.iot.service;

import java.util.List;

import com.aquacomunidad.backend.features.iot.dto.LecturaIotSolicitudDto;
import com.aquacomunidad.backend.features.iot.dto.NivelAguaDto;

public interface IotServicio {
  List<NivelAguaDto> listarNiveles();

  NivelAguaDto registrarLectura(LecturaIotSolicitudDto request);
}
