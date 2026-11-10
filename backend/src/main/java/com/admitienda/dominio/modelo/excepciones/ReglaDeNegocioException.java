package com.admitienda.dominio.modelo.excepciones;

/**
 * Un dato de entrada o una regla de negocio no se cumplen
 * (equivale a una respuesta HTTP 400).
 */
public class ReglaDeNegocioException extends ExcepcionDeDominio {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
