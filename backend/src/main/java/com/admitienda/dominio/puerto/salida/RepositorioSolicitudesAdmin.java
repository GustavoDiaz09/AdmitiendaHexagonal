package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.SolicitudAdmin;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de solicitudes de permiso de administrador.
 */
public interface RepositorioSolicitudesAdmin {

    /** Inserta una solicitud nueva y le asigna identificador. */
    SolicitudAdmin guardar(SolicitudAdmin solicitud);

    /** Actualiza una solicitud ya persistida. */
    SolicitudAdmin actualizar(SolicitudAdmin solicitud);

    /** Busca una solicitud por identificador. */
    Optional<SolicitudAdmin> buscarPorId(long id);

    /** Lista todas las solicitudes, primero las PENDIENTE. */
    List<SolicitudAdmin> listarTodas();

    /** Indica si el usuario ya tiene una solicitud PENDIENTE. */
    boolean existePendienteDeUsuario(long usuarioId);

    /** Elimina todas las solicitudes de un usuario (al borrar su cuenta). */
    void eliminarPorUsuario(long usuarioId);
}
