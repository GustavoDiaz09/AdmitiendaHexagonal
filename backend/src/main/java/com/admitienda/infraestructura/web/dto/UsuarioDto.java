package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.modelo.Usuario;

/**
 * Usuario en el formato del contrato de la API.
 */
public record UsuarioDto(long id, String nombreUsuario, String rol, long fechaRegistro) {

    /** Traduce el usuario del dominio al formato de la API. */
    public static UsuarioDto desde(Usuario usuario) {
        return new UsuarioDto(usuario.id(), usuario.nombreUsuario(),
                usuario.rol().name(), usuario.fechaRegistro());
    }
}
