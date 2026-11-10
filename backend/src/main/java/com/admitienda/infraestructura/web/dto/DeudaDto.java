package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.puerto.entrada.GestionDeDeudas;

import java.util.List;

/**
 * Deuda con sus abonos en el formato del contrato.
 */
public record DeudaDto(long id, double monto, double saldo, String descripcion,
                       long fecha, List<PagoDto> pagos) {

    /** Traduce la deuda con sus pagos al formato de la API. */
    public static DeudaDto desde(GestionDeDeudas.DeudaConPagos deudaConPagos) {
        var deuda = deudaConPagos.deuda();
        return new DeudaDto(deuda.id(), deuda.monto().pesos(), deuda.saldo().pesos(),
                deuda.descripcion(), deuda.fecha(),
                deudaConPagos.pagos().stream().map(PagoDto::desde).toList());
    }
}
