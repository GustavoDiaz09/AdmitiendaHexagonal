package com.admitienda.infraestructura.persistencia;

import com.admitienda.dominio.modelo.Deuda;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.puerto.salida.RepositorioDeudas;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: deudas sobre SQLite.
 */
public final class AdaptadorDeudasJdbc implements RepositorioDeudas {

    private final JdbcTemplate jdbc;

    public AdaptadorDeudasJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Deuda guardar(Deuda deuda) {
        String sql = """
                INSERT INTO deudas (deudor_id, cliente_nombre, monto_cent, saldo_cent,
                                    descripcion, fecha)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Long id = Insertar.conLlaveGenerada(jdbc, sql, deuda.deudorId(), deuda.clienteNombre(),
                deuda.monto().centavos(), deuda.saldo().centavos(), deuda.descripcion(),
                deuda.fecha());
        return new Deuda(id, deuda.deudorId(), deuda.clienteNombre(), deuda.monto(),
                deuda.saldo(), deuda.descripcion(), deuda.fecha());
    }

    @Override
    public Deuda actualizar(Deuda deuda) {
        jdbc.update("""
                UPDATE deudas
                SET monto_cent = ?, saldo_cent = ?, descripcion = ?
                WHERE id = ?
                """, deuda.monto().centavos(), deuda.saldo().centavos(),
                deuda.descripcion(), deuda.id());
        return deuda;
    }

    @Override
    public Optional<Deuda> buscarPorId(long id) {
        return jdbc.query("SELECT * FROM deudas WHERE id = ?", this::aDeuda, id)
                .stream().findFirst();
    }

    @Override
    public List<Deuda> listarPorDeudor(long deudorId) {
        return jdbc.query("SELECT * FROM deudas WHERE deudor_id = ? ORDER BY fecha DESC",
                this::aDeuda, deudorId);
    }

    @Override
    public Dinero totalPendiente() {
        Long total = jdbc.queryForObject(
                "SELECT COALESCE(SUM(saldo_cent), 0) FROM deudas", Long.class);
        return Dinero.deCentavos(total == null ? 0 : total);
    }

    @Override
    public void eliminar(long id) {
        jdbc.update("DELETE FROM deudas WHERE id = ?", id);
    }

    private Deuda aDeuda(ResultSet fila, int numero) throws SQLException {
        return new Deuda(fila.getLong("id"), fila.getLong("deudor_id"),
                fila.getString("cliente_nombre"), Dinero.deCentavos(fila.getLong("monto_cent")),
                Dinero.deCentavos(fila.getLong("saldo_cent")),
                fila.getString("descripcion"), fila.getLong("fecha"));
    }
}
