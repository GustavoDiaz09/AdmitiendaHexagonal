package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.modelo.Producto;

/**
 * Producto en el formato del contrato de la API. El precio de venta va
 * calculado para que el cliente nunca reciba datos inconsistentes.
 */
public record ProductoDto(long id, String tipoProducto, String nombreProducto,
                          double precioNeto, double ganancia, double precioVenta,
                          int cantidadStock, int stockMinimo, boolean enAlerta) {

    /** Traduce el producto del dominio al formato de la API. */
    public static ProductoDto desde(Producto producto) {
        return new ProductoDto(producto.id(), producto.tipoProducto(), producto.nombreProducto(),
                producto.precioNeto().pesos(), producto.ganancia().pesos(),
                producto.precioVenta().pesos(), producto.cantidadStock(),
                producto.stockMinimo(), producto.enAlerta());
    }
}
