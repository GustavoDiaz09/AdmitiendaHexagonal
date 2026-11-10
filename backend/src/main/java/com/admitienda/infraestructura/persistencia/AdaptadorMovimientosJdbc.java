package com.admitienda.infraestructura.persistencia;

import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.TipoMovimiento;
import com.admitienda.dominio.puerto.salida.RepositorioMovimientos;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: movimientos de caja sobre SQLite. Los rangos de
 * fecha aceptan extremos nulos ("desde siempre" / "hasta siempre").
 */
public final class AdaptadorMovimientosJdbc implements RepositorioMovimientos {

    private final JdbcTemplate jdbc;

    public AdaptadorMovimientosJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Movimiento guardar(Movimiento movimiento) {
        String sql = """
                INSERT INTO movimientos (tipo_movimiento, monto_cent, descripcion, fecha)
                VALUES (?, ?, ?, ?)
                """;
        Long id = Insertar.conLlaveGenerada(jdbc, sql, movimiento.tipo().name(),
                movimiento.monto().centavos(), movimiento.descripcion(), movimiento.fecha());
        return new Movimiento(id, movimiento.tipo(), movimiento.monto(),
                movimiento.descripcion(), movimiento.fecha());
    }

    @Override
    public Optional<Movimiento> buscarPorId(long id) {
        return jdbc.query("SELECT * FROM movimientos WHERE id = ?", this::aMovimiento, id)
                .stream().findFirst();
    }

    @Override
    public List<Movimiento> buscarEntre(Long desde, Long hasta) {
        return jdbc.query("""
                        SELECT * FROM movimientos
                        WHERE (? IS NULL OR fecha >= ?) AND (? IS NULL OR fecha <= ?)
                        ORDER BY fecha DESC
                """, this::aMovimiento, desde, desde, hasta, hasta);
    }

    @Override
    public Dinero totalDe(TipoMovimiento tipo) {
        Long total = jdbc.queryForObject(
                "SELECT COALESCE(SUM(monto_cent), 0) FROM movimientos WHERE tipo_movimiento = ?",
                Long.class, tipo.name());
        return Dinero.deCentavos(total == null ? 0 : total);
    }

    @Override
    public void eliminar(long id) {
        jdbc.update("DELETE FROM movimientos WHERE id = ?", id);
    }

    private Movimiento aMovimiento(ResultSet fila, int numero) throws SQLException {
        return new Movimiento(fila.getLong("id"),
                TipoMovimiento.valueOf(fila.getString("tipo_movimiento")),
                Dinero.deCentavos(fila.getLong("monto_cent")),
                fila.getString("descripcion"), fila.getLong("fecha"));
    }
}
