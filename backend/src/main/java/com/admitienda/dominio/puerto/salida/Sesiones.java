package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Usuario;

import java.util.Optional;

/**
 * Puerto de salida: almacén de sesiones activas por token opaco.
 */
public interface Sesiones {

    /** Abre una sesión para el usuario y devuelve su token. */
    String abrir(Usuario usuario);

    /** Cierra la sesión de un token (idempotente). */
    void cerrar(String token);

    /**
     * Cierra todas las sesiones de un usuario. Se usa cuando cambia su rol
     * o su cuenta se elimina, para que el token no conserve permisos viejos.
     */
    void cerrarDeUsuario(long usuarioId);

    /** Resuelve el actor autenticado de un token, si sigue vigente. */
    Optional<Actor> actorDe(String token);
}
