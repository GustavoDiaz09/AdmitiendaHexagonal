package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.puerto.entrada.GestionDeDeudas;

/**
 * Deudor con el resumen de su cartera en el formato del contrato.
 */
public record DeudorResumenDto(long id, String nombreDeudor, int deudasPendientes,
                               double totalPendiente, long fechaUltimaDeuda) {

    /** Traduce el resumen del dominio al formato de la API. */
    public static DeudorResumenDto desde(GestionDeDeudas.ResumenDeDeudor resumen) {
        return new DeudorResumenDto(resumen.deudor().id(), resumen.deudor().nombreDeudor(),
                resumen.deudasPendientes(), resumen.totalPendiente().pesos(),
                resumen.fechaUltimaDeuda());
    }
}
