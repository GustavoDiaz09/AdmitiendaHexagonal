package com.admitienda.infraestructura.persistencia;

import com.admitienda.dominio.modelo.Deudor;
import com.admitienda.dominio.puerto.salida.RepositorioDeudores;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: catálogo de deudores sobre SQLite. La unicidad por
 * nombre normalizado la garantiza la tabla; el caso de uso la verifica
 * antes para dar un mensaje claro.
 */
public final class AdaptadorDeudoresJdbc implements RepositorioDeudores {

    private final JdbcTemplate jdbc;

    public AdaptadorDeudoresJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Deudor guardar(Deudor deudor) {
        Long id = Insertar.conLlaveGenerada(jdbc,
                "INSERT INTO deudores (nombre_deudor, nombre_normalizado) VALUES (?, ?)",
                deudor.nombreDeudor(), deudor.nombreNormalizado());
        return new Deudor(id, deudor.nombreDeudor(), deudor.nombreNormalizado());
    }

    @Override
    public Optional<Deudor> buscarPorId(long id) {
        return jdbc.query("SELECT * FROM deudores WHERE id = ?", this::aDeudor, id)
                .stream().findFirst();
    }

    @Override
    public Optional<Deudor> buscarPorNombreNormalizado(String nombreNormalizado) {
        return jdbc.query("SELECT * FROM deudores WHERE nombre_normalizado = ?",
                        this::aDeudor, nombreNormalizado)
                .stream().findFirst();
    }

    @Override
    public List<Deudor> listarTodos() {
        return jdbc.query("SELECT * FROM deudores ORDER BY nombre_deudor", this::aDeudor);
    }

    private Deudor aDeudor(ResultSet fila, int numero) throws SQLException {
        return new Deudor(fila.getLong("id"), fila.getString("nombre_deudor"),
                fila.getString("nombre_normalizado"));
    }
}
