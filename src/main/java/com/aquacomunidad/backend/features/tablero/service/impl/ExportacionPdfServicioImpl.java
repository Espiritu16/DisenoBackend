package com.aquacomunidad.backend.features.tablero.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aquacomunidad.backend.features.tablero.dto.TableroKpiDto;
import com.aquacomunidad.backend.features.tablero.service.ExportacionPdfServicio;

@Service
public class ExportacionPdfServicioImpl implements ExportacionPdfServicio {

  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @Override
  public byte[] generarReporteDashboard(TableroKpiDto kpis) {
    String contenido = contenidoReporte(kpis);
    return construirPdf(contenido);
  }

  private String contenidoReporte(TableroKpiDto kpis) {
    List<String> lineas = new ArrayList<>();
    lineas.add("AquaComunidad - Reporte operativo");
    lineas.add("Fecha: " + FORMATO_FECHA.format(LocalDate.now()));
    lineas.add("");
    lineas.add("Reportes pendientes: " + kpis.getReportesPendientes());
    lineas.add("Reportes en proceso: " + kpis.getReportesEnProceso());
    lineas.add("Reportes resueltos: " + kpis.getReportesResueltos());
    lineas.add("Casos abiertos: " + kpis.getCasosAbiertos());
    lineas.add("Casos resueltos: " + kpis.getCasosResueltos());
    lineas.add("Promedio de resolucion (horas): " + kpis.getPromedioHorasResolucion());
    lineas.add("");
    lineas.add("Zonas con mayor incidencia:");
    kpis.getReportesPorZona().forEach(zona -> lineas.add("- " + zona.getNombre() + ": " + zona.getCantidad()));
    lineas.add("");
    lineas.add("Niveles de agua:");
    kpis.getNivelesAgua().forEach(nivel -> lineas.add("- " + nivel.getNombre() + " / "
        + nivel.getZona() + ": " + nivel.getNivelPorcentaje() + "% (" + nivel.getEstado() + ")"));
    return String.join("\n", lineas);
  }

  private byte[] construirPdf(String contenido) {
    String stream = "BT\n/F1 12 Tf\n50 780 Td\n"
        + escaparTextoPdf(contenido).replace("\n", ") Tj\n0 -18 Td\n(")
        + ") Tj\nET";
    String objeto1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
    String objeto2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";
    String objeto3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
        + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n";
    String objeto4 = "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n";
    String objeto5 = "5 0 obj\n<< /Length " + stream.getBytes(StandardCharsets.ISO_8859_1).length
        + " >>\nstream\n" + stream + "\nendstream\nendobj\n";

    List<String> objetos = List.of(objeto1, objeto2, objeto3, objeto4, objeto5);
    StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
    List<Integer> offsets = new ArrayList<>();
    for (String objeto : objetos) {
      offsets.add(pdf.length());
      pdf.append(objeto);
    }
    int xrefOffset = pdf.length();
    pdf.append("xref\n0 6\n0000000000 65535 f \n");
    for (Integer offset : offsets) {
      pdf.append(String.format("%010d 00000 n %n", offset));
    }
    pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n")
        .append(xrefOffset)
        .append("\n%%EOF");
    return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
  }

  private String escaparTextoPdf(String texto) {
    return "(" + texto
        .replace("\\", "\\\\")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replaceAll("[^\\x09\\x0A\\x0D\\x20-\\x7E]", "");
  }
}
