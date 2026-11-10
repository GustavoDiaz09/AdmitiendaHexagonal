package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de productos.
 */
public interface RepositorioProductos {

    /** Inserta un producto nuevo y le asigna identificador. */
    Producto guardar(Producto producto);

    /** Actualiza un producto ya persistido. */
    Producto actualizar(Producto producto);

    /** Busca un producto por identificador. */
    Optional<Producto> buscarPorId(long id);

    /**
     * Lista los productos cuyo nombre contiene el fragmento (insensible a
     * mayúsculas); un fragmento vacío lista todos.
     */
    List<Producto> buscarPorNombre(String fragmento);

    /** Elimina el producto por identificador. */
    void eliminar(long id);
}
