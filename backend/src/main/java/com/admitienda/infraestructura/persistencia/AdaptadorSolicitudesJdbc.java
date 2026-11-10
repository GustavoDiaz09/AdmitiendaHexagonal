package com.admitienda.infraestructura.persistencia;

import com.admitienda.dominio.modelo.EstadoSolicitud;
import com.admitienda.dominio.modelo.SolicitudAdmin;
import com.admitienda.dominio.puerto.salida.RepositorioSolicitudesAdmin;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: solicitudes de permiso sobre SQLite. El nombre del
 * solicitante se resuelve con un JOIN para que el dominio no necesite una
 * segunda consulta.
 */
public final class AdaptadorSolicitudesJdbc implements RepositorioSolicitudesAdmin {

    private static final String CONSULTA_BASE = """
            SELECT s.id, s.usuario_id, s.estado, s.motivo, s.fecha_solicitud, u.nombre_usuario
            FROM solicitudes_admin s
            JOIN usuarios u ON u.id = s.usuario_id
            """;

    private final JdbcTemplate jdbc;

    public AdaptadorSolicitudesJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public SolicitudAdmin guardar(SolicitudAdmin solicitud) {
        String sql = """
                INSERT INTO solicitudes_admin (usuario_id, estado, motivo, fecha_solicitud)
                VALUES (?, ?, ?, ?)
                """;
        Long id = Insertar.conLlaveGenerada(jdbc, sql, solicitud.usuarioId(),
                solicitud.estado().name(), solicitud.motivo(), solicitud.fechaSolicitud());
        return new SolicitudAdmin(id, solicitud.usuarioId(), solicitud.nombreUsuario(),
                solicitud.estado(), solicitud.motivo(), solicitud.fechaSolicitud());
    }

    @Override
    public SolicitudAdmin actualizar(SolicitudAdmin solicitud) {
        jdbc.update("UPDATE solicitudes_admin SET estado = ? WHERE id = ?",
                solicitud.estado().name(), solicitud.id());
        return solicitud;
    }

    @Override
    public Optional<SolicitudAdmin> buscarPorId(long id) {
        return jdbc.query(CONSULTA_BASE + " WHERE s.id = ?", this::aSolicitud, id)
                .stream().findFirst();
    }

    @Override
    public List<SolicitudAdmin> listarTodas() {
        return jdbc.query(CONSULTA_BASE + """
                         ORDER BY CASE s.estado WHEN 'PENDIENTE' THEN 0 ELSE 1 END,
                                  s.fecha_solicitud DESC
                """, this::aSolicitud);
    }

    @Override
    public boolean existePendienteDeUsuario(long usuarioId) {
        Boolean existe = jdbc.queryForObject(
                "SELECT COUNT(*) > 0 FROM solicitudes_admin WHERE usuario_id = ? AND estado = 'PENDIENTE'",
                Boolean.class, usuarioId);
        return Boolean.TRUE.equals(existe);
    }

    @Override
    public void eliminarPorUsuario(long usuarioId) {
        jdbc.update("DELETE FROM solicitudes_admin WHERE usuario_id = ?", usuarioId);
    }

    private SolicitudAdmin aSolicitud(ResultSet fila, int numero) throws SQLException {
        return new SolicitudAdmin(fila.getLong("id"), fila.getLong("usuario_id"),
                fila.getString("nombre_usuario"), EstadoSolicitud.valueOf(fila.getString("estado")),
                fila.getString("motivo"), fila.getLong("fecha_solicitud"));
    }
}
