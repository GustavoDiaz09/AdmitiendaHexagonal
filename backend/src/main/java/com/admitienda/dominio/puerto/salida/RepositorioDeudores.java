package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.Deudor;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de deudores (catálogo de clientes fiados).
 */
public interface RepositorioDeudores {

    /** Inserta un deudor nuevo y le asigna identificador. */
    Deudor guardar(Deudor deudor);

    /** Busca un deudor por identificador. */
    Optional<Deudor> buscarPorId(long id);

    /** Busca un deudor por nombre normalizado (clave de unicidad). */
    Optional<Deudor> buscarPorNombreNormalizado(String nombreNormalizado);

    /** Lista todos los deudores por nombre. */
    List<Deudor> listarTodos();
}
