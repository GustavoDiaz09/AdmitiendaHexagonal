package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.Deuda;
import com.admitienda.dominio.modelo.Dinero;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de deudas.
 */
public interface RepositorioDeudas {

    /** Inserta una deuda nueva y le asigna identificador. */
    Deuda guardar(Deuda deuda);

    /** Actualiza una deuda ya persistida. */
    Deuda actualizar(Deuda deuda);

    /** Busca una deuda por identificador. */
    Optional<Deuda> buscarPorId(long id);

    /** Lista las deudas de un deudor, de la más reciente a la más antigua. */
    List<Deuda> listarPorDeudor(long deudorId);

    /** Suma de saldos pendientes de toda la cartera. */
    Dinero totalPendiente();

    /** Elimina la deuda por identificador. */
    void eliminar(long id);
}
