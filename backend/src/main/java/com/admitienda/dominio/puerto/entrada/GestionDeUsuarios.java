package com.admitienda.dominio.puerto.entrada;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.Usuario;

import java.util.List;

/**
 * Puerto de entrada: alta de cuentas y administración de usuarios.
 */
public interface GestionDeUsuarios {

    /**
     * Registra una cuenta nueva con rol REGISTRADO.
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si el nombre o la contraseña no cumplen las reglas.
     * @throws com.admitienda.dominio.modelo.excepciones.ConflictoDeDatosException
     *         si el nombre de usuario ya está ocupado.
     */
    Usuario registrar(RegistroDeUsuario datos);

    /** Lista todas las cuentas (requiere rol ADMIN). */
    List<Usuario> listar(Actor actor);

    /**
     * Cambia el rol de una cuenta (requiere rol ADMIN).
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si el cambio dejaría la tienda sin administradores.
     */
    Usuario cambiarRol(Actor actor, long idUsuario, TipoUsuario nuevoRol);

    /**
     * Elimina una cuenta y sus solicitudes de permiso (requiere rol ADMIN).
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si se intenta eliminar al último administrador.
     */
    void eliminar(Actor actor, long idUsuario);

    /** Datos de registro de una cuenta nueva. */
    record RegistroDeUsuario(String nombreUsuario, String contrasena, String indicio) {
    }
}
