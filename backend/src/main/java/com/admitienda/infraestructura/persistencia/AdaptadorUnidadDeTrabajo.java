package com.admitienda.infraestructura.persistencia;

import com.admitienda.dominio.puerto.salida.UnidadDeTrabajo;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Adaptador de salida: transacciones de Spring aplicadas al puerto de
 * unidad de trabajo. El dominio pide atomicidad; este adaptador decide
 * con qué mecanismo cumplirla.
 */
public final class AdaptadorUnidadDeTrabajo implements UnidadDeTrabajo {

    private final TransactionTemplate transaccion;

    public AdaptadorUnidadDeTrabajo(TransactionTemplate transaccion) {
        this.transaccion = transaccion;
    }

    @Override
    public <T> T ejecutar(Supplier<T> bloque) {
        return transaccion.execute(estado -> bloque.get());
    }
}
