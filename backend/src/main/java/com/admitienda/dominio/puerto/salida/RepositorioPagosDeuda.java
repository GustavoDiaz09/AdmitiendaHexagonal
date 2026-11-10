package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.PagoDeuda;

import java.util.List;

/**
 * Puerto de salida: persistencia de abonos a deudas.
 */
public interface RepositorioPagosDeuda {

    /** Inserta un abono nuevo y le asigna identificador. */
    PagoDeuda guardar(PagoDeuda pago);

    /** Lista los abonos de una deuda, del más reciente al más antiguo. */
    List<PagoDeuda> listarPorDeuda(long deudaId);

    /** Elimina todos los abonos de una deuda. */
    void eliminarPorDeuda(long deudaId);
}
