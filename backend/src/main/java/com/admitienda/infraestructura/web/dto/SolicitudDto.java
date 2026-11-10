package com.admitienda.infraestructura.web.dto;

import com.admitienda.dominio.modelo.SolicitudAdmin;

/**
 * Solicitud de permiso de administrador en el formato del contrato.
 */
public record SolicitudDto(long id, long usuarioId, String nombreUsuario, String estado,
                           long fechaSolicitud, String motivo) {

    /** Traduce la solicitud del dominio al formato de la API. */
    public static SolicitudDto desde(SolicitudAdmin solicitud) {
        return new SolicitudDto(solicitud.id(), solicitud.usuarioId(),
                solicitud.nombreUsuario(), solicitud.estado().name(),
                solicitud.fechaSolicitud(), solicitud.motivo());
    }
}
