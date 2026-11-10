package com.admitienda.dominio.modelo;

/**
 * Roles que puede tener una cuenta de usuario en la tienda.
 */
public enum TipoUsuario {

    /** Solo puede consultar el catálogo de productos. */
    INVITADO,

    /** Puede leer los datos de la tienda y solicitar permiso de administrador. */
    REGISTRADO,

    /** Acceso completo a la gestión de la tienda. */
    ADMIN;

    /** Indica si el rol puede gestionar la tienda (administrar). */
    public boolean esAdministrador() {
        return this == ADMIN;
    }
}
