package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de usuarios.
 */
public interface RepositorioUsuarios {

    /** Inserta un usuario nuevo y le asigna identificador. */
    Usuario guardar(Usuario usuario);

    /** Actualiza un usuario ya persistido. */
    Usuario actualizar(Usuario usuario);

    /** Busca un usuario por identificador. */
    Optional<Usuario> buscarPorId(long id);

    /** Busca un usuario por nombre normalizado (clave de unicidad). */
    Optional<Usuario> buscarPorNombreNormalizado(String nombreNormalizado);

    /** Lista todas las cuentas. */
    List<Usuario> listarTodos();

    /** Indica si ya existe una cuenta con ese nombre normalizado. */
    boolean existeNombreNormalizado(String nombreNormalizado);

    /** Cantidad de cuentas con rol ADMIN (para proteger al último administrador). */
    int contarAdministradores();

    /** Elimina la cuenta por identificador. */
    void eliminar(long id);
}
