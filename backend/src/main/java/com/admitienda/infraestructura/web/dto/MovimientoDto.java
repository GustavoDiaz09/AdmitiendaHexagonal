package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.modelo.Movimiento;

/**
 * Movimiento de caja en el formato del contrato de la API.
 */
public record MovimientoDto(long id, String tipo, double monto,
                            String descripcion, long fecha) {

    /** Traduce el movimiento del dominio al formato de la API. */
    public static MovimientoDto desde(Movimiento movimiento) {
        return new MovimientoDto(movimiento.id(), movimiento.tipo().name(),
                movimiento.monto().pesos(), movimiento.descripcion(), movimiento.fecha());
    }
}
