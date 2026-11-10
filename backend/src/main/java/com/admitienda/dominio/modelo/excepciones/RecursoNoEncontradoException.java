package com.admitienda.dominio.modelo.excepciones;

/**
 * El recurso solicitado no existe o ya fue eliminado
 * (equivale a una respuesta HTTP 404).
 */
public class RecursoNoEncontradoException extends ExcepcionDeDominio {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
