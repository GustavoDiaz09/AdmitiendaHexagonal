package com.admitienda.infraestructura.persistencia;

import com.admitienda.dominio.compartido.Validaciones;
import com.admitienda.dominio.modelo.Credenciales;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.Usuario;
import com.admitienda.dominio.puerto.salida.RepositorioUsuarios;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: usuarios sobre SQLite mediante JdbcTemplate.
 */
public final class AdaptadorUsuariosJdbc implements RepositorioUsuarios {

    private final JdbcTemplate jdbc;

    public AdaptadorUsuariosJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        String sql = """
                INSERT INTO usuarios (nombre_usuario, nombre_normalizado, rol, indicio,
                                      sal_contrasena, hash_contrasena, fecha_registro)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        Long id = Insertar.conLlaveGenerada(jdbc, sql, usuario.nombreUsuario(),
                Validaciones.normalizarNombre(usuario.nombreUsuario()), usuario.rol().name(),
                usuario.indicio(), usuario.credenciales().sal(), usuario.credenciales().hash(),
                usuario.fechaRegistro());
        return new Usuario(id, usuario.nombreUsuario(), usuario.rol(), usuario.indicio(),
                usuario.fechaRegistro(), usuario.credenciales());
    }

    @Override
    public Usuario actualizar(Usuario usuario) {
        jdbc.update("""
                UPDATE usuarios
                SET nombre_usuario = ?, rol = ?, indicio = ?, sal_contrasena = ?, hash_contrasena = ?
                WHERE id = ?
                """, usuario.nombreUsuario(), usuario.rol().name(), usuario.indicio(),
                usuario.credenciales().sal(), usuario.credenciales().hash(), usuario.id());
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return jdbc.query("SELECT * FROM usuarios WHERE id = ?", this::aUsuario, id)
                .stream().findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorNombreNormalizado(String nombreNormalizado) {
        return jdbc.query("SELECT * FROM usuarios WHERE nombre_normalizado = ?",
                        this::aUsuario, nombreNormalizado)
                .stream().findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        return jdbc.query("SELECT * FROM usuarios ORDER BY nombre_usuario", this::aUsuario);
    }

    @Override
    public boolean existeNombreNormalizado(String nombreNormalizado) {
        Boolean existe = jdbc.queryForObject(
                "SELECT COUNT(*) > 0 FROM usuarios WHERE nombre_normalizado = ?",
                Boolean.class, nombreNormalizado);
        return Boolean.TRUE.equals(existe);
    }

    @Override
    public int contarAdministradores() {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN'", Integer.class);
        return cantidad == null ? 0 : cantidad;
    }

    @Override
    public void eliminar(long id) {
        jdbc.update("DELETE FROM usuarios WHERE id = ?", id);
    }

    private Usuario aUsuario(ResultSet fila, int numero) throws SQLException {
        return new Usuario(fila.getLong("id"), fila.getString("nombre_usuario"),
                TipoUsuario.valueOf(fila.getString("rol")), fila.getString("indicio"),
                fila.getLong("fecha_registro"), new Credenciales(
                fila.getString("sal_contrasena"), fila.getString("hash_contrasena")));
    }
}
