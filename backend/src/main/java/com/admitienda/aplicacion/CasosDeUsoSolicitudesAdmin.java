package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.EstadoSolicitud;
import com.admitienda.dominio.modelo.SolicitudAdmin;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.excepciones.ConflictoDeDatosException;
import com.admitienda.dominio.modelo.excepciones.PermisoDenegadoException;
import com.admitienda.dominio.modelo.excepciones.RecursoNoEncontradoException;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeSolicitudesAdmin;
import com.admitienda.dominio.puerto.salida.RepositorioSolicitudesAdmin;
import com.admitienda.dominio.puerto.salida.RepositorioUsuarios;
import com.admitienda.dominio.puerto.salida.Reloj;
import com.admitienda.dominio.puerto.salida.UnidadDeTrabajo;

import java.util.List;

/**
 * Caso de uso: solicitud y decisión de permisos de administrador.
 */
public final class CasosDeUsoSolicitudesAdmin implements GestionDeSolicitudesAdmin {

    private final RepositorioSolicitudesAdmin repositorioSolicitudes;
    private final RepositorioUsuarios repositorioUsuarios;
    private final Reloj reloj;
    private final UnidadDeTrabajo unidadDeTrabajo;

    public CasosDeUsoSolicitudesAdmin(RepositorioSolicitudesAdmin repositorioSolicitudes,
                                       RepositorioUsuarios repositorioUsuarios,
                                       Reloj reloj, UnidadDeTrabajo unidadDeTrabajo) {
        this.repositorioSolicitudes = repositorioSolicitudes;
        this.repositorioUsuarios = repositorioUsuarios;
        this.reloj = reloj;
        this.unidadDeTrabajo = unidadDeTrabajo;
    }

    @Override
    public SolicitudAdmin solicitar(Actor actor, String motivo) {
        if (actor.esAdministrador()) {
            throw new ReglaDeNegocioException(
                    "Los administradores no necesitan solicitar el permiso.");
        }
        if (actor.rol() != TipoUsuario.REGISTRADO) {
            throw new PermisoDenegadoException(
                    "Solo un usuario registrado puede solicitar el permiso de administrador.");
        }
        if (repositorioSolicitudes.existePendienteDeUsuario(actor.id())) {
            throw new ConflictoDeDatosException("Ya tienes una solicitud pendiente.");
        }
        String motivoLimpio = motivo == null || motivo.isBlank() ? "" : motivo.trim();
        SolicitudAdmin nueva = new SolicitudAdmin(null, actor.id(), actor.nombreUsuario(),
                EstadoSolicitud.PENDIENTE, motivoLimpio, reloj.ahora());
        return repositorioSolicitudes.guardar(nueva);
    }

    @Override
    public List<SolicitudAdmin> listar(Actor actor) {
        ReglasDeAcceso.exigirAdministrador(actor, "ver las solicitudes de permiso");
        return repositorioSolicitudes.listarTodas();
    }

    @Override
    public SolicitudAdmin aprobar(Actor actor, long idSolicitud) {
        ReglasDeAcceso.exigirAdministrador(actor, "decidir las solicitudes");
        SolicitudAdmin solicitud = repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("La solicitud no existe."));
        if (solicitud.estado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaDeNegocioException("La solicitud ya fue resuelta.");
        }
        var solicitante = repositorioUsuarios.buscarPorId(solicitud.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El solicitante ya no existe."));
        return unidadDeTrabajo.ejecutar(() -> {
            repositorioUsuarios.actualizar(solicitante.conRol(TipoUsuario.ADMIN));
            return repositorioSolicitudes.actualizar(
                    solicitud.conEstado(EstadoSolicitud.APROBADA));
        });
    }

    @Override
    public SolicitudAdmin rechazar(Actor actor, long idSolicitud) {
        ReglasDeAcceso.exigirAdministrador(actor, "decidir las solicitudes");
        SolicitudAdmin solicitud = repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("La solicitud no existe."));
        if (solicitud.estado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaDeNegocioException("La solicitud ya fue resuelta.");
        }
        return repositorioSolicitudes.actualizar(solicitud.conEstado(EstadoSolicitud.RECHAZADA));
    }
}
