package com.admitienda.dominio.modelo;

/**
 * Estado del ciclo de vida de una solicitud de permiso de administrador.
 */
public enum EstadoSolicitud {

    /** Espera la decisión de un administrador. */
    PENDIENTE,

    /** Fue aprobada: el solicitante fue promovido a ADMIN. */
    APROBADA,

    /** Fue rechazada: el rol del solicitante no cambia. */
    RECHAZADA
}
