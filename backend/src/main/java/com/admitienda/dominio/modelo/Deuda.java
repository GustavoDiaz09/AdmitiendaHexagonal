package com.admitienda.dominio.modelo;

/**
 * Deuda de un deudor (venta fiada). El saldo decrece con cada abono; una
 * deuda con saldo cero está saldada.
 *
 * @param id            identificador, {@code null} mientras no se persiste.
 * @param deudorId      deudor que debe pagar.
 * @param clienteNombre nombre visible del deudor al momento de la venta.
 * @param monto         valor original de la deuda.
 * @param saldo         valor pendiente por pagar.
 * @param descripcion   detalle de lo que se llevó fiado.
 * @param fecha         fecha de la venta en milisegundos epoch.
 */
public record Deuda(Long id, Long deudorId, String clienteNombre, Dinero monto,
                    Dinero saldo, String descripcion, long fecha) {

    /** Lo que ya se ha abonado sobre la deuda. */
    public Dinero abonado() {
        return monto.restar(saldo);
    }

    /** Indica si la deuda ya está completamente pagada. */
    public boolean estaSaldada() {
        return saldo.esCero();
    }

    /** Devuelve una copia de la deuda con otro saldo. */
    public Deuda conSaldo(Dinero nuevoSaldo) {
        return new Deuda(id, deudorId, clienteNombre, monto, nuevoSaldo, descripcion, fecha);
    }

    /** Devuelve una copia con nuevo monto, saldo y descripción (edición). */
    public Deuda conDetalle(Dinero nuevoMonto, Dinero nuevoSaldo, String nuevaDescripcion) {
        return new Deuda(id, deudorId, clienteNombre, nuevoMonto, nuevoSaldo,
                nuevaDescripcion, fecha);
    }
}
