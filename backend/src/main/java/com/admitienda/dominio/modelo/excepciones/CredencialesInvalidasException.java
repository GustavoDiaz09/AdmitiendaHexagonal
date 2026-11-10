package com.admitienda.dominio.modelo.excepciones;

/**
 * El usuario o la contraseña no son correctos al iniciar sesión
 * (equivale a una respuesta HTTP 401, sin revelar cuál de los dos falló).
 */
public class CredencialesInvalidasException extends ExcepcionDeDominio {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
