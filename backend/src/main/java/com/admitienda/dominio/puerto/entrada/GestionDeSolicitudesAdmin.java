package com.admitienda.dominio.puerto.entrada;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.SolicitudAdmin;

import java.util.List;

/**
 * Puerto de entrada: solicitud, decisión y listado de permisos de
 * administrador.
 */
public interface GestionDeSolicitudesAdmin {

    /**
     * Crea una solicitud PENDIENTE a nombre del actor (solo REGISTRADO).
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si el actor no puede solicitar el permiso.
     * @throws com.admitienda.dominio.modelo.excepciones.ConflictoDeDatosException
     *         si ya existe una solicitud pendiente del actor.
     */
    SolicitudAdmin solicitar(Actor actor, String motivo);

    /** Lista todas las solicitudes, primero las PENDIENTE (requiere ADMIN). */
    List<SolicitudAdmin> listar(Actor actor);

    /**
     * Aprueba una solicitud: el solicitante pasa a ADMIN de forma atómica
     * junto con el cambio de estado (requiere ADMIN).
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si la solicitud ya fue resuelta.
     */
    SolicitudAdmin aprobar(Actor actor, long idSolicitud);

    /** Rechaza una solicitud sin cambiar el rol del solicitante (requiere ADMIN). */
    SolicitudAdmin rechazar(Actor actor, long idSolicitud);
}
