package com.admitienda.infraestructura.persistencia;

import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Producto;
import com.admitienda.dominio.puerto.salida.RepositorioProductos;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: productos sobre SQLite. LIKE de SQLite es
 * insensible a mayúsculas, de modo que el filtro por nombre es tolerante.
 */
public final class AdaptadorProductosJdbc implements RepositorioProductos {

    private final JdbcTemplate jdbc;

    public AdaptadorProductosJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Producto guardar(Producto producto) {
        String sql = """
                INSERT INTO productos (tipo_producto, nombre_producto, precio_neto_cent,
                                      ganancia_cent, cantidad_stock, stock_minimo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Long id = Insertar.conLlaveGenerada(jdbc, sql, producto.tipoProducto(),
                producto.nombreProducto(), producto.precioNeto().centavos(),
                producto.ganancia().centavos(), producto.cantidadStock(), producto.stockMinimo());
        return new Producto(id, producto.tipoProducto(), producto.nombreProducto(),
                producto.precioNeto(), producto.ganancia(), producto.cantidadStock(),
                producto.stockMinimo());
    }

    @Override
    public Producto actualizar(Producto producto) {
        jdbc.update("""
                UPDATE productos
                SET tipo_producto = ?, nombre_producto = ?, precio_neto_cent = ?,
                    ganancia_cent = ?, cantidad_stock = ?, stock_minimo = ?
                WHERE id = ?
                """, producto.tipoProducto(), producto.nombreProducto(),
                producto.precioNeto().centavos(), producto.ganancia().centavos(),
                producto.cantidadStock(), producto.stockMinimo(), producto.id());
        return producto;
    }

    @Override
    public Optional<Producto> buscarPorId(long id) {
        return jdbc.query("SELECT * FROM productos WHERE id = ?", this::aProducto, id)
                .stream().findFirst();
    }

    @Override
    public List<Producto> buscarPorNombre(String fragmento) {
        return jdbc.query("""
                        SELECT * FROM productos
                        WHERE nombre_producto LIKE '%' || ? || '%'
                        ORDER BY nombre_producto
                """, this::aProducto, fragmento);
    }

    @Override
    public void eliminar(long id) {
        jdbc.update("DELETE FROM productos WHERE id = ?", id);
    }

    private Producto aProducto(ResultSet fila, int numero) throws SQLException {
        return new Producto(fila.getLong("id"), fila.getString("tipo_producto"),
                fila.getString("nombre_producto"),
                Dinero.deCentavos(fila.getLong("precio_neto_cent")),
                Dinero.deCentavos(fila.getLong("ganancia_cent")),
                fila.getInt("cantidad_stock"), fila.getInt("stock_minimo"));
    }
}
