package com.admitienda.dominio.puerto.salida;

/**
 * Puerto de salida: fuente de la hora actual. Inyectarlo como puerto
 * permite que las pruebas determinen el tiempo (reloj fijo) sin tocar
 * relojes del sistema.
 */
public interface Reloj {

    /** Hora actual en milisegundos epoch. */
    long ahora();
}
