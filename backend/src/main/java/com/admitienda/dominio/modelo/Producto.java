package com.admitienda.dominio.modelo;

/**
 * Producto de la tienda. El precio de venta no se persiste: se deriva de
 * sumar el precio neto y la ganancia, para que no existan datos duplicados
 * que puedan quedar inconsistentes.
 *
 * @param id             identificador, {@code null} mientras no se persiste.
 * @param tipoProducto   categoría del producto.
 * @param nombreProducto nombre visible.
 * @param precioNeto     costo del producto para la tienda.
 * @param ganancia       utilidad que se suma al costo.
 * @param cantidadStock  unidades disponibles en inventario.
 * @param stockMinimo    umbral a partir del cual el producto entra en alerta.
 */
public record Producto(Long id, String tipoProducto, String nombreProducto,
                        Dinero precioNeto, Dinero ganancia,
                        int cantidadStock, int stockMinimo) {

    /** Precio de venta derivado: costo más ganancia. */
    public Dinero precioVenta() {
        return precioNeto.sumar(ganancia);
    }

    /** Indica si el producto está en alerta de inventario bajo. */
    public boolean enAlerta() {
        return cantidadStock <= stockMinimo;
    }
}
