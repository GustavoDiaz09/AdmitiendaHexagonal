package com.admitienda.aplicacion;

import com.admitienda.dominio.compartido.Validaciones;
import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.SesionIniciada;
import com.admitienda.dominio.modelo.Usuario;
import com.admitienda.dominio.modelo.excepciones.CredencialesInvalidasException;
import com.admitienda.dominio.puerto.entrada.GestionDeSesiones;
import com.admitienda.dominio.puerto.salida.CifradorDeContrasenas;
import com.admitienda.dominio.puerto.salida.RepositorioUsuarios;
import com.admitienda.dominio.puerto.salida.Sesiones;

import java.util.Optional;

/**
 * Caso de uso: autenticación de usuarios.
 *
 * <p>Las credenciales inválidas siempre producen el mismo mensaje para no
 * revelar si la cuenta existe o no.</p>
 */
public final class CasosDeUsoSesiones implements GestionDeSesiones {

    private final RepositorioUsuarios repositorioUsuarios;
    private final CifradorDeContrasenas cifrador;
    private final Sesiones sesiones;

    public CasosDeUsoSesiones(RepositorioUsuarios repositorioUsuarios,
                              CifradorDeContrasenas cifrador, Sesiones sesiones) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.cifrador = cifrador;
        this.sesiones = sesiones;
    }

    @Override
    public SesionIniciada iniciarSesion(String nombreUsuario, String contrasena) {
        Usuario usuario = repositorioUsuarios
                .buscarPorNombreNormalizado(Validaciones.normalizarNombre(nombreUsuario))
                .orElseThrow(() -> new CredencialesInvalidasException(
                        "Usuario o contraseña incorrectos."));
        if (!cifrador.verificar(contrasena, usuario.credenciales())) {
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
        }
        return new SesionIniciada(sesiones.abrir(usuario), usuario);
    }

    @Override
    public void cerrarSesion(String token) {
        sesiones.cerrar(token);
    }

    @Override
    public Optional<Actor> actorDelToken(String token) {
        return sesiones.actorDe(token);
    }
}
