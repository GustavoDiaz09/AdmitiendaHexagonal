package com.admitienda.dominio.puerto.salida;

import java.util.function.Supplier;

/**
 * Puerto de salida: agrupa varias escrituras en una sola unidad atómica:
 * o se aplican todas, o ninguna. Evita que el dominio dependa de la
 * tecnología de transacciones (aquí JDBC, mañana lo que sea).
 */
public interface UnidadDeTrabajo {

    /** Ejecuta un bloque dentro de una transacción devolviendo su resultado. */
    <T> T ejecutar(Supplier<T> bloque);

    /** Variante sin resultado de {@link #ejecutar(Supplier)}. */
    default void ejecutar(Runnable bloque) {
        ejecutar(() -> {
            bloque.run();
            return null;
        });
    }
}
