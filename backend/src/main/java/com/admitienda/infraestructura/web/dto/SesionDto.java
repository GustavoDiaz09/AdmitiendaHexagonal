package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.modelo.SesionIniciada;

/**
 * Respuesta de iniciar sesión: token opaco y usuario autenticado.
 */
public record SesionDto(String token, UsuarioDto usuario) {

    /** Traduce la sesión iniciada del dominio al formato de la API. */
    public static SesionDto desde(SesionIniciada sesion) {
        return new SesionDto(sesion.token(), UsuarioDto.desde(sesion.usuario()));
    }
}
