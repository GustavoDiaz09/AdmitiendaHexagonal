package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.modelo.Actor;

/**
 * Usuario autenticado reportado por {@code GET /api/sesiones/actual}.
 */
public record UsuarioActualDto(long id, String nombreUsuario, String rol) {

    /** Traduce el actor de la sesión al formato de la API. */
    public static UsuarioActualDto desde(Actor actor) {
        return new UsuarioActualDto(actor.id(), actor.nombreUsuario(), actor.rol().name());
    }
}
