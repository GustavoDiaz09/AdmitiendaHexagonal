package com.admitienda.aplicacion;

import com.admitienda.dominio.compartido.Validaciones;
import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Credenciales;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.Usuario;
import com.admitienda.dominio.modelo.excepciones.ConflictoDeDatosException;
import com.admitienda.dominio.modelo.excepciones.RecursoNoEncontradoException;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeUsuarios;
import com.admitienda.dominio.puerto.salida.CifradorDeContrasenas;
import com.admitienda.dominio.puerto.salida.RepositorioSolicitudesAdmin;
import com.admitienda.dominio.puerto.salida.RepositorioUsuarios;
import com.admitienda.dominio.puerto.salida.Reloj;
import com.admitienda.dominio.puerto.salida.Sesiones;
import com.admitienda.dominio.puerto.salida.UnidadDeTrabajo;

import java.util.List;

/**
 * Caso de uso: alta de cuentas y administración de usuarios.
 */
public final class CasosDeUsoUsuarios implements GestionDeUsuarios {

    private static final int LONGITUD_MINIMA_NOMBRE = 3;
    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioSolicitudesAdmin repositorioSolicitudes;
    private final CifradorDeContrasenas cifrador;
    private final Reloj reloj;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final Sesiones sesiones;

    public CasosDeUsoUsuarios(RepositorioUsuarios repositorioUsuarios,
                              RepositorioSolicitudesAdmin repositorioSolicitudes,
                              CifradorDeContrasenas cifrador, Reloj reloj,
                              UnidadDeTrabajo unidadDeTrabajo, Sesiones sesiones) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioSolicitudes = repositorioSolicitudes;
        this.cifrador = cifrador;
        this.reloj = reloj;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.sesiones = sesiones;
    }

    @Override
    public Usuario registrar(RegistroDeUsuario datos) {
        String nombre = Validaciones.requerirTexto(datos.nombreUsuario(), "nombre de usuario");
        if (nombre.length() < LONGITUD_MINIMA_NOMBRE) {
            throw new ReglaDeNegocioException(
                    "El nombre de usuario debe tener al menos %d caracteres."
                            .formatted(LONGITUD_MINIMA_NOMBRE));
        }
        String contrasena = Validaciones.requerirTexto(datos.contrasena(), "contraseña");
        if (contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ReglaDeNegocioException(
                    "La contraseña debe tener al menos %d caracteres."
                            .formatted(LONGITUD_MINIMA_CONTRASENA));
        }
        String normalizado = Validaciones.normalizarNombre(nombre);
        if (repositorioUsuarios.existeNombreNormalizado(normalizado)) {
            throw new ConflictoDeDatosException("El nombre de usuario ya está ocupado.");
        }
        Credenciales credenciales = cifrador.cifrar(contrasena);
        Usuario nuevo = new Usuario(null, nombre, TipoUsuario.REGISTRADO,
                datos.indicio() == null ? "" : datos.indicio().trim(), reloj.ahora(), credenciales);
        return repositorioUsuarios.guardar(nuevo);
    }

    @Override
    public List<Usuario> listar(Actor actor) {
        ReglasDeAcceso.exigirAdministrador(actor, "ver las cuentas");
        return repositorioUsuarios.listarTodos();
    }

    @Override
    public Usuario cambiarRol(Actor actor, long idUsuario, TipoUsuario nuevoRol) {
        ReglasDeAcceso.exigirAdministrador(actor, "cambiar roles");
        Usuario usuario = repositorioUsuarios.buscarPorId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));
        if (usuario.rol() == nuevoRol) {
            return usuario;
        }
        if (usuario.esAdministrador() && !nuevoRol.esAdministrador()
                && repositorioUsuarios.contarAdministradores() <= 1) {
            throw new ReglaDeNegocioException("No puede quedar la tienda sin administradores.");
        }
        Usuario actualizado = repositorioUsuarios.actualizar(usuario.conRol(nuevoRol));
        sesiones.cerrarDeUsuario(idUsuario);
        return actualizado;
    }

    @Override
    public void eliminar(Actor actor, long idUsuario) {
        ReglasDeAcceso.exigirAdministrador(actor, "eliminar cuentas");
        Usuario usuario = repositorioUsuarios.buscarPorId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));
        if (usuario.esAdministrador() && repositorioUsuarios.contarAdministradores() <= 1) {
            throw new ReglaDeNegocioException("No puede quedar la tienda sin administradores.");
        }
        unidadDeTrabajo.ejecutar(() -> {
            repositorioSolicitudes.eliminarPorUsuario(idUsuario);
            repositorioUsuarios.eliminar(idUsuario);
        });
        sesiones.cerrarDeUsuario(idUsuario);
    }
}
