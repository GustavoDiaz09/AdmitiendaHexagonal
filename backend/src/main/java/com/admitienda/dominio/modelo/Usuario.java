package com.admitienda.dominio.modelo;

/**
 * Cuenta de usuario de la tienda.
 *
 * @param id             identificador, {@code null} mientras no se persiste.
 * @param nombreUsuario  nombre visible elegido por el usuario.
 * @param rol            rol de la cuenta.
 * @param indicio        texto de ayuda para recordar la contraseña.
 * @param fechaRegistro  fecha de creación en milisegundos epoch.
 * @param credenciales   sal y hash de la contraseña.
 */
public record Usuario(Long id, String nombreUsuario, TipoUsuario rol,
                      String indicio, long fechaRegistro, Credenciales credenciales) {

    /** Indica si la cuenta tiene rol administrativo. */
    public boolean esAdministrador() {
        return rol.esAdministrador();
    }

    /** Devuelve una copia del usuario con otro rol. */
    public Usuario conRol(TipoUsuario nuevoRol) {
        return new Usuario(id, nombreUsuario, nuevoRol, indicio, fechaRegistro, credenciales);
    }
}
