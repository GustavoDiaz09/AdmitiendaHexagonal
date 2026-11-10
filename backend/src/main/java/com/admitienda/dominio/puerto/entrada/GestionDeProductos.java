package com.admitienda.dominio.puerto.entrada;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Producto;

import java.util.List;

/**
 * Puerto de entrada: catálogo de productos de la tienda.
 */
public interface GestionDeProductos {

    /**
     * Lista los productos cuyo nombre contiene el fragmento dado (vacío o
     * {@code null} lista todos).
     */
    List<Producto> listar(String fragmentoDeNombre);

    /**
     * Consulta un producto por su identificador.
     *
     * @throws com.admitienda.dominio.modelo.excepciones.RecursoNoEncontradoException
     *         si el producto no existe.
     */
    Producto consultarPorId(long id);

    /**
     * Crea un producto (requiere rol ADMIN).
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si algún dato no cumple las reglas.
     */
    Producto crear(Actor actor, ProductoPorCrear datos);

    /** Actualiza un producto existente (requiere rol ADMIN). */
    Producto actualizar(Actor actor, long id, ProductoPorCrear datos);

    /** Elimina un producto (requiere rol ADMIN). */
    void eliminar(Actor actor, long id);

    /** Datos necesarios para crear o editar un producto. */
    record ProductoPorCrear(String tipoProducto, String nombreProducto,
                            com.admitienda.dominio.modelo.Dinero precioNeto,
                            com.admitienda.dominio.modelo.Dinero ganancia,
                            int cantidadStock, int stockMinimo) {
    }
}
