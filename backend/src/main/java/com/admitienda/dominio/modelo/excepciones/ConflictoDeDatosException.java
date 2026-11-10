package com.admitienda.dominio.modelo.excepciones;

/**
 * El dato viola una unicidad de identidad en el dominio
 * (equivale a una respuesta HTTP 409).
 */
public class ConflictoDeDatosException extends ExcepcionDeDominio {

    public ConflictoDeDatosException(String mensaje) {
        super(mensaje);
    }
}
