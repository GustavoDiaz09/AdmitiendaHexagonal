package com.admitienda.dominio.puerto.entrada;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Dinero;

import java.util.List;

/**
 * Puerto de entrada: resumen financiero de la tienda (solo consulta).
 */
public interface ConsultaDeResumenes {

    /**
     * Calcula el resumen general: saldo de caja, total por cobrar del
     * fiado, totales de los últimos 7 días y de los últimos 12 meses
     * (hora Colombia).
     */
    ResumenGeneral obtenerResumen(Actor actor);

    /** Totales de un día concreto. */
    record ResumenPorDia(String fecha, Dinero ingresos, Dinero egresos, Dinero neto) {
    }

    /** Totales de un mes concreto. */
    record ResumenPorMes(String mes, Dinero ingresos, Dinero egresos, Dinero neto) {
    }

    /** Resumen general de la tienda. */
    record ResumenGeneral(Dinero saldoCaja, Dinero totalPorCobrar,
                          List<ResumenPorDia> porDia, List<ResumenPorMes> porMes) {
    }
}
