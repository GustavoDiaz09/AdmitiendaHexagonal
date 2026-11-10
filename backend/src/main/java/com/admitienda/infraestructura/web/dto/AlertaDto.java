package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.modelo.Producto;

/**
 * Producto en alerta de inventario bajo, con el faltante hasta el mínimo.
 */
public record AlertaDto(long id, String nombreProducto, String tipoProducto,
                        int cantidadStock, int stockMinimo, int faltante) {

    /** Traduce el producto en alerta al formato de la API. */
    public static AlertaDto desde(Producto producto) {
        return new AlertaDto(producto.id(), producto.nombreProducto(), producto.tipoProducto(),
                producto.cantidadStock(), producto.stockMinimo(),
                Math.max(0, producto.stockMinimo() - producto.cantidadStock()));
    }
}
