package com.admitienda.dominio.puerto.entrada;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Producto;

import java.util.List;

/**
 * Puerto de entrada: alertas de inventario bajo (solo consulta).
 */
public interface ConsultaDeAlertas {

    /** Lista los productos cuya cantidad en stock cayó a su mínimo o menos. */
    List<Producto> productosConStockBajo(Actor actor);
}
