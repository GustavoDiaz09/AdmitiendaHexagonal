package com.admitienda.dominio.modelo;

/**
 * Movimiento de caja: una entrada (ingreso) o una salida (egreso) de dinero.
 *
 * @param id          identificador, {@code null} mientras no se persiste.
 * @param tipo        INGRESO o EGRESO.
 * @param monto       importe del movimiento, siempre positivo.
 * @param descripcion concepto legible del movimiento.
 * @param fecha       fecha en milisegundos epoch (la fija el servidor).
 */
public record Movimiento(Long id, TipoMovimiento tipo, Dinero monto,
                         String descripcion, long fecha) {
}
