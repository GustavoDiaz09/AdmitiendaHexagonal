package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.puerto.entrada.GestionDeDeudas;

import java.util.List;

/**
 * Historial de un deudor: sus deudas y los abonos de cada una.
 */
public record HistorialDeDeudorDto(long id, String nombreDeudor, List<DeudaDto> deudas) {

    /** Traduce el historial del dominio al formato de la API. */
    public static HistorialDeDeudorDto desde(GestionDeDeudas.HistorialDeDeudor historial) {
        return new HistorialDeDeudorDto(historial.deudor().id(),
                historial.deudor().nombreDeudor(),
                historial.deudas().stream().map(DeudaDto::desde).toList());
    }
}
