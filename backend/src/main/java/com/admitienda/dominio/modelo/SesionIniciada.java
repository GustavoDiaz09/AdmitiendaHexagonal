package com.admitienda.dominio.modelo;

/**
 * Resultado de iniciar sesión: el token opaco que el frontend debe enviar
 * en la cabecera {@code Authorization} y el usuario que acaba de entrar.
 *
 * @param token   token de sesión.
 * @param usuario usuario autenticado.
 */
public record SesionIniciada(String token, Usuario usuario) {
}
