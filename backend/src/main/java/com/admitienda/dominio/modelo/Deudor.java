package com.admitienda.dominio.modelo;

/**
 * Deudor: cliente al que se le vende fiado. Su identidad es única por
 * nombre normalizado, de modo que el mismo cliente siempre acumule su
 * cartera aunque se escriba distinto ("María Pérez", "maría   pérez").
 *
 * @param id               identificador, {@code null} mientras no se persiste.
 * @param nombreDeudor     nombre visible con el que se registró.
 * @param nombreNormalizado clave de unicidad (ver
 *                          {@link com.admitienda.dominio.compartido.Validaciones#normalizarNombre}).
 */
public record Deudor(Long id, String nombreDeudor, String nombreNormalizado) {
}
