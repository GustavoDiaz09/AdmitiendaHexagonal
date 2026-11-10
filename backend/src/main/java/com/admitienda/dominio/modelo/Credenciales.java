package com.admitienda.dominio.modelo;

/**
 * Objeto de valor con el material criptográfico de la contraseña de un
 * usuario: la sal aleatoria y el hash derivado de ella.
 *
 * @param sal   sal aleatoria en hexadecimal.
 * @param hash  hash de la contraseña en el formato {@code pbkdf2$iteraciones$sal$hash}.
 */
public record Credenciales(String sal, String hash) {
}
