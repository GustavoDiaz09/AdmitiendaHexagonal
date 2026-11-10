package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Producto;
import com.admitienda.dominio.puerto.entrada.ConsultaDeAlertas;
import com.admitienda.dominio.puerto.salida.RepositorioProductos;

import java.util.List;

/**
 * Caso de uso: alertas de inventario bajo.
 */
public final class CasosDeUsoAlertas implements ConsultaDeAlertas {

    private final RepositorioProductos productos;

    public CasosDeUsoAlertas(RepositorioProductos productos) {
        this.productos = productos;
    }

    @Override
    public List<Producto> productosConStockBajo(Actor actor) {
        ReglasDeAcceso.exigirRegistrado(actor, "ver las alertas");
        return productos.buscarPorNombre("").stream()
                .filter(Producto::enAlerta)
                .toList();
    }
}
