package com.admitienda.dominio.modelo;

/**
 * Usuario autenticado que dispara un caso de uso. Viaja desde el filtro
 * de sesión hasta los casos de uso para que las reglas de acceso se
 * decidan dentro del hexágono y no en el adaptador HTTP.
 *
 * @param id            identificador del usuario.
 * @param nombreUsuario nombre visible de la cuenta.
 * @param rol           rol con el que inició sesión.
 */
public record Actor(Long id, String nombreUsuario, TipoUsuario rol) {

    /** Indica si el actor tiene rol administrativo. */
    public boolean esAdministrador() {
        return rol.esAdministrador();
    }
}
