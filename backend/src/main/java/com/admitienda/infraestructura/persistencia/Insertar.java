package com.admitienda.infraestructura.persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Utilidad de los adaptadores JDBC para ejecutar INSERT y recuperar la
 * llave generada por SQLite (last_insert_rowid).
 */
final class Insertar {

    private Insertar() {
    }

    /** Ejecuta el INSERT y devuelve el identificador autogenerado. */
    static Long conLlaveGenerada(JdbcTemplate jdbc, String sql, Object... argumentos) {
        KeyHolder llave = new GeneratedKeyHolder();
        jdbc.update(conexion -> {
            PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < argumentos.length; i++) {
                sentencia.setObject(i + 1, argumentos[i]);
            }
            return sentencia;
        }, llave);
        Number identificador = llave.getKey();
        if (identificador == null) {
            throw new IllegalStateException("La inserción no generó identificador: " + sql);
        }
        return identificador.longValue();
    }
}
