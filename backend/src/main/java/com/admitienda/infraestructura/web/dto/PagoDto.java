package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.modelo.PagoDeuda;

/**
 * Abono a una deuda en el formato del contrato.
 */
public record PagoDto(long id, double monto, long fecha) {

    /** Traduce el abono del dominio al formato de la API. */
    public static PagoDto desde(PagoDeuda pago) {
        return new PagoDto(pago.id(), pago.monto().pesos(), pago.fecha());
    }
}
