package com.admitienda.dominio.puerto.entrada;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.SesionIniciada;

import java.util.Optional;

/**
 * Puerto de entrada: autenticación de usuarios y ciclo de vida de su sesión.
 */
public interface GestionDeSesiones {

    /**
     * Verifica las credenciales y abre una sesión.
     *
     * @throws com.admitienda.dominio.modelo.excepciones.CredencialesInvalidasException
     *         si el usuario no existe o la contraseña no coincide.
     */
    SesionIniciada iniciarSesion(String nombreUsuario, String contrasena);

    /** Cierra la sesión asociada a un token (idempotente). */
    void cerrarSesion(String token);

    /** Resuelve el actor autenticado detrás de un token de sesión. */
    Optional<Actor> actorDelToken(String token);
}
