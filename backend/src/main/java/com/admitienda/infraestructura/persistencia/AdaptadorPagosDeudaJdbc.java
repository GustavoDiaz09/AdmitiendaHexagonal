package com.admitienda.infraestructura.persistencia;

import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.PagoDeuda;
import com.admitienda.dominio.puerto.salida.RepositorioPagosDeuda;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Adaptador de salida: abonos a deudas sobre SQLite.
 */
public final class AdaptadorPagosDeudaJdbc implements RepositorioPagosDeuda {

    private final JdbcTemplate jdbc;

    public AdaptadorPagosDeudaJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PagoDeuda guardar(PagoDeuda pago) {
        String sql = """
                INSERT INTO pagos_deuda (deuda_id, monto_cent, descripcion, fecha)
                VALUES (?, ?, ?, ?)
                """;
        Long id = Insertar.conLlaveGenerada(jdbc, sql, pago.deudaId(), pago.monto().centavos(),
                pago.descripcion(), pago.fecha());
        return new PagoDeuda(id, pago.deudaId(), pago.monto(), pago.descripcion(), pago.fecha());
    }

    @Override
    public List<PagoDeuda> listarPorDeuda(long deudaId) {
        return jdbc.query("SELECT * FROM pagos_deuda WHERE deuda_id = ? ORDER BY fecha DESC",
                this::aPago, deudaId);
    }

    @Override
    public void eliminarPorDeuda(long deudaId) {
        jdbc.update("DELETE FROM pagos_deuda WHERE deuda_id = ?", deudaId);
    }

    private PagoDeuda aPago(ResultSet fila, int numero) throws SQLException {
        return new PagoDeuda(fila.getLong("id"), fila.getLong("deuda_id"),
                Dinero.deCentavos(fila.getLong("monto_cent")),
                fila.getString("descripcion"), fila.getLong("fecha"));
    }
}
