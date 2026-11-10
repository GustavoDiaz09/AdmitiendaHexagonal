package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.Usuario;
import com.admitienda.dominio.modelo.excepciones.CredencialesInvalidasException;
import com.admitienda.dominio.modelo.SesionIniciada;
import com.admitienda.dominio.puerto.entrada.GestionDeSesiones;
import com.admitienda.dominio.puerto.salida.Sesiones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del caso de uso de sesiones con un almacén de sesiones falso.
 */
class CasosDeUsoSesionesTest {

    private final Falsos.UsuariosEnMemoria usuarios = new Falsos.UsuariosEnMemoria();
    private final Falsos.CifradorFalso cifrador = new Falsos.CifradorFalso();

    private GestionDeSesiones casos;

    @BeforeEach
    void preparar() {
        casos = new CasosDeUsoSesiones(usuarios, cifrador, new SesionesFalsas());
    }

    @Test
    void iniciarSesionDevuelveTokenYUsuario() {
        usuarios.guardar(new Usuario(null, "admin", TipoUsuario.ADMIN, "", 1L,
                cifrador.cifrar("admin123")));

        SesionIniciada sesion = casos.iniciarSesion("Admin", "admin123");

        assertThat(sesion.token()).isNotBlank();
        assertThat(sesion.usuario().nombreUsuario()).isEqualTo("admin");
    }

    @Test
    void iniciarSesionRechazaContrasenaIncorrecta() {
        usuarios.guardar(new Usuario(null, "admin", TipoUsuario.ADMIN, "", 1L,
                cifrador.cifrar("admin123")));

        assertThatThrownBy(() -> casos.iniciarSesion("admin", "otra123"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void iniciarSesionRechazaUsuarioInexistente() {
        assertThatThrownBy(() -> casos.iniciarSesion("nadie", "loquesea"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    /** Sesiones falsas: token fijo, actor guardado en un mapa. */
    private static final class SesionesFalsas implements Sesiones {
        private final Map<String, Actor> vivas = new HashMap<>();

        @Override
        public String abrir(Usuario usuario) {
            String token = "token-" + usuario.id();
            vivas.put(token, new Actor(usuario.id(), usuario.nombreUsuario(), usuario.rol()));
            return token;
        }

        @Override
        public void cerrar(String token) {
            vivas.remove(token);
        }

        @Override
        public Optional<Actor> actorDe(String token) {
            return Optional.ofNullable(vivas.get(token));
        }
    }
}
