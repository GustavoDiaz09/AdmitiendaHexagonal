package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.puerto.entrada.ConsultaDeResumenes.ResumenGeneral;
import com.admitienda.dominio.puerto.entrada.ConsultaDeResumenes.ResumenPorDia;
import com.admitienda.dominio.puerto.entrada.ConsultaDeResumenes.ResumenPorMes;

import java.util.List;

/**
 * Resumen financiero de la tienda en el formato del contrato.
 */
public record ResumenDto(double saldoCaja, double totalPorCobrar,
                         List<PorDiaDto> porDia, List<PorMesDto> porMes) {

    /** Traduce el resumen del dominio al formato de la API. */
    public static ResumenDto desde(ResumenGeneral resumen) {
        return new ResumenDto(resumen.saldoCaja().pesos(), resumen.totalPorCobrar().pesos(),
                resumen.porDia().stream().map(PorDiaDto::desde).toList(),
                resumen.porMes().stream().map(PorMesDto::desde).toList());
    }

    /** Totales de un día. */
    public record PorDiaDto(String fecha, double ingresos, double egresos, double neto) {

        static PorDiaDto desde(ResumenPorDia dia) {
            return new PorDiaDto(dia.fecha(), dia.ingresos().pesos(),
                    dia.egresos().pesos(), dia.neto().pesos());
        }
    }

    /** Totales de un mes. */
    public record PorMesDto(String mes, double ingresos, double egresos, double neto) {

        static PorMesDto desde(ResumenPorMes mes) {
            return new PorMesDto(mes.mes(), mes.ingresos().pesos(),
                    mes.egresos().pesos(), mes.neto().pesos());
        }
    }
}
