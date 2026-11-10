package com.admitienda.aplicacion;

import com.admitienda.dominio.compartido.Validaciones;
import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Producto;
import com.admitienda.dominio.modelo.excepciones.RecursoNoEncontradoException;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeProductos;
import com.admitienda.dominio.puerto.salida.RepositorioProductos;

import java.util.List;

/**
 * Caso de uso: catálogo de productos.
 */
public final class CasosDeUsoProductos implements GestionDeProductos {

    private final RepositorioProductos repositorio;

    public CasosDeUsoProductos(RepositorioProductos repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Producto> listar(String fragmentoDeNombre) {
        String fragmento = fragmentoDeNombre == null ? "" : fragmentoDeNombre.trim();
        return repositorio.buscarPorNombre(fragmento);
    }

    @Override
    public Producto consultarPorId(long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto no existe."));
    }

    @Override
    public Producto crear(Actor actor, ProductoPorCrear datos) {
        ReglasDeAcceso.exigirAdministrador(actor, "crear productos");
        return repositorio.guardar(validar(null, datos));
    }

    @Override
    public Producto actualizar(Actor actor, long id, ProductoPorCrear datos) {
        ReglasDeAcceso.exigirAdministrador(actor, "editar productos");
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto no existe."));
        return repositorio.actualizar(validar(id, datos));
    }

    @Override
    public void eliminar(Actor actor, long id) {
        ReglasDeAcceso.exigirAdministrador(actor, "eliminar productos");
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto no existe."));
        repositorio.eliminar(id);
    }

    /** Valida los datos y construye el producto listo para persistir. */
    private Producto validar(Long id, ProductoPorCrear datos) {
        String tipo = Validaciones.requerirTexto(datos.tipoProducto(), "tipo de producto");
        String nombre = Validaciones.requerirTexto(datos.nombreProducto(), "nombre del producto");
        if (!datos.precioNeto().esPositivo()) {
            throw new ReglaDeNegocioException("El campo precio neto debe ser mayor a 0.");
        }
        Validaciones.exigirNoNegativo(datos.cantidadStock(), "cantidad en stock");
        Validaciones.exigirNoNegativo(datos.stockMinimo(), "stock mínimo");
        return new Producto(id, tipo, nombre, datos.precioNeto(), datos.ganancia(),
                datos.cantidadStock(), datos.stockMinimo());
    }
}
