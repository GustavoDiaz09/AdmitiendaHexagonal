package com.admitienda.dominio.modelo.excepciones;

/**
 * La cuenta autenticada no tiene el rol exigido por la operación
 * (equivale a una respuesta HTTP 403).
 */
public class PermisoDenegadoException extends ExcepcionDeDominio {

    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
