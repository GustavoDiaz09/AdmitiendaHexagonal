package com.admitienda.dominio.modelo;

/**
 * Abono aplicado a una deuda. Cada abono genera además un ingreso en caja.
 *
 * @param id          identificador, {@code null} mientras no se persiste.
 * @param deudaId     deuda a la que abona.
 * @param monto       valor abonado, siempre positivo.
 * @param descripcion detalle del abono.
 * @param fecha       fecha en milisegundos epoch.
 */
public record PagoDeuda(Long id, Long deudaId, Dinero monto, String descripcion, long fecha) {
}
