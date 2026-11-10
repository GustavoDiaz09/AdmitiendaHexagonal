package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.puerto.entrada.GestionDeMovimientos;

import java.util.List;

/**
 * Listado de movimientos junto con el saldo histórico de la caja.
 */
public record ListadoDeMovimientosDto(double saldoCaja, List<MovimientoDto> movimientos) {

    /** Traduce el listado del dominio al formato de la API. */
    public static ListadoDeMovimientosDto desde(GestionDeMovimientos.ListadoDeMovimientos listado) {
        return new ListadoDeMovimientosDto(listado.saldoCaja().pesos(),
                listado.movimientos().stream().map(MovimientoDto::desde).toList());
    }
}
