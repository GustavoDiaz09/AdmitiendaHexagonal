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

    /** Resuelve el actor autenticado de un token, si sigue vigente. */
    Optional<Actor> actorDe(String token);
}
