package com.admitienda.dominio.modelo;

/**
 * Solicitud de permiso de administrador creada por un usuario registrado.
 *
 * @param id             identificador, {@code null} mientras no se persiste.
 * @param usuarioId      solicitante.
 * @param nombreUsuario  nombre visible del solicitante (para listados).
 * @param estado         PENDIENTE, APROBADA o RECHAZADA.
 * @param motivo         texto libre del solicitante.
 * @param fechaSolicitud fecha en milisegundos epoch.
 */
public record SolicitudAdmin(Long id, Long usuarioId, String nombreUsuario,
                              EstadoSolicitud estado, String motivo, long fechaSolicitud) {

    /** Devuelve una copia de la solicitud con otro estado. */
    public SolicitudAdmin conEstado(EstadoSolicitud nuevoEstado) {
        return new SolicitudAdmin(id, usuarioId, nombreUsuario, nuevoEstado, motivo, fechaSolicitud);
    }
}
