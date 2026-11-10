package com.admitienda.dominio.modelo.excepciones;

/**
 * Raíz de las excepciones del dominio. El hexágono nunca devuelve códigos
 * de error: expresa sus fallos con excepciones con mensaje legible y deja
 * que el adaptador de entrada decida cómo traducirlas a HTTP.
 */
public abstract class ExcepcionDeDominio extends RuntimeException {

    protected ExcepcionDeDominio(String mensaje) {
        super(mensaje);
    }
}
