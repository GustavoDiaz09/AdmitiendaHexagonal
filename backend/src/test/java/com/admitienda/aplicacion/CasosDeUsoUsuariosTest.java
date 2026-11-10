package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.Usuario;
import com.admitienda.dominio.modelo.excepciones.ConflictoDeDatosException;
import com.admitienda.dominio.modelo.excepciones.PermisoDenegadoException;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas de los casos de uso de cuentas de usuario: registro y las
 * reglas que protegen a los administradores.
 */
class CasosDeUsoUsuariosTest {

    private final Falsos.UsuariosEnMemoria usuarios = new Falsos.UsuariosEnMemoria();
    private final Falsos.SolicitudesEnMemoria solicitudes = new Falsos.SolicitudesEnMemoria();
    private final Falsos.CifradorFalso cifrador = new Falsos.CifradorFalso();
    private final Falsos.RelojFijo reloj = new Falsos.RelojFijo();
    private final Falsos.SesionesEnMemoria sesiones = new Falsos.SesionesEnMemoria();

    private GestionDeUsuarios casos;

    @BeforeEach
    void preparar() {
        casos = new CasosDeUsoUsuarios(usuarios, solicitudes, cifrador, reloj,
                new Falsos.UnidadDirecta(), sesiones);
    }

    @Test
    void registrarCreaUnUsuarioRegistrado() {
        Usuario nuevo = casos.registrar(
                new GestionDeUsuarios.RegistroDeUsuario("Ana", "ana12345", "mi gato"));

        assertThat(nuevo.id()).isNotNull();
        assertThat(nuevo.rol()).isEqualTo(TipoUsuario.REGISTRADO);
        assertThat(cifrador.verificar("ana12345", nuevo.credenciales())).isTrue();
        assertThat(usuarios.listarTodos()).hasSize(1);
    }

    @Test
    void registrarRechazaNombreCorto() {
        assertThatThrownBy(() -> casos.registrar(
                new GestionDeUsuarios.RegistroDeUsuario("an", "ana12345", null)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("al menos");
    }

    @Test
    void registrarRechazaContrasenaCorta() {
        assertThatThrownBy(() -> casos.registrar(
                new GestionDeUsuarios.RegistroDeUsuario("ana", "123", null)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("contraseña");
    }

    @Test
    void registrarRechazaNombreOcupadoSinImportarMayusculas() {
        casos.registrar(new GestionDeUsuarios.RegistroDeUsuario("Ana", "ana12345", null));

        assertThatThrownBy(() -> casos.registrar(
                new GestionDeUsuarios.RegistroDeUsuario("  ANA ", "otra12345", null)))
                .isInstanceOf(ConflictoDeDatosException.class)
                .hasMessageContaining("ocupado");
    }

    @Test
    void listarExigeAdministrador() {
        Actor registrado = actor(TipoUsuario.REGISTRADO);

        assertThatThrownBy(() -> casos.listar(registrado))
                .isInstanceOf(PermisoDenegadoException.class);
    }

    @Test
    void cambiarRolNoDejaLaTiendaSinAdministradores() {
        Usuario admin = registrarAdmin("admin");
        Actor actorAdmin = new Actor(admin.id(), "admin", TipoUsuario.ADMIN);

        assertThatThrownBy(() -> casos.cambiarRol(actorAdmin, admin.id(),
                TipoUsuario.REGISTRADO))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("sin administradores");
    }

    @Test
    void cambiarRolFuncionaCuandoQuedaOtroAdministrador() {
        Usuario admin1 = registrarAdmin("admin1");
        registrarAdmin("admin2");
        Actor actor = new Actor(admin1.id(), "admin1", TipoUsuario.ADMIN);

        Usuario cambiado = casos.cambiarRol(actor, 2L, TipoUsuario.REGISTRADO);

        assertThat(cambiado.rol()).isEqualTo(TipoUsuario.REGISTRADO);
    }

    @Test
    void eliminarBorraAlUsuarioYSusSolicitudes() {
        Usuario admin = registrarAdmin("admin");
        Usuario ana = casos.registrar(
                new GestionDeUsuarios.RegistroDeUsuario("ana", "ana12345", null));
        Actor actorAdmin = new Actor(admin.id(), "admin", TipoUsuario.ADMIN);

        casos.eliminar(actorAdmin, ana.id());

        assertThat(usuarios.buscarPorId(ana.id())).isEmpty();
        assertThat(usuarios.listarTodos()).hasSize(1);
    }

    @Test
    void cambiarRolCierraLasSesionesDelUsuario() {
        Usuario admin1 = registrarAdmin("admin1");
        Usuario otro = registrarAdmin("admin2");
        Actor actor = new Actor(admin1.id(), "admin1", TipoUsuario.ADMIN);
        String token = sesiones.abrir(otro);

        casos.cambiarRol(actor, otro.id(), TipoUsuario.REGISTRADO);

        assertThat(sesiones.actorDe(token)).isEmpty();
    }

    @Test
    void eliminarCierraLasSesionesDelUsuario() {
        Usuario admin = registrarAdmin("admin");
        Usuario ana = casos.registrar(
                new GestionDeUsuarios.RegistroDeUsuario("ana", "ana12345", null));
        String token = sesiones.abrir(ana);

        casos.eliminar(new Actor(admin.id(), "admin", TipoUsuario.ADMIN), ana.id());

        assertThat(sesiones.actorDe(token)).isEmpty();
    }

    @Test
    void eliminarAlUltimoAdministradorEstaProhibido() {
        Usuario admin = registrarAdmin("admin");
        Actor actorAdmin = new Actor(admin.id(), "admin", TipoUsuario.ADMIN);

        assertThatThrownBy(() -> casos.eliminar(actorAdmin, admin.id()))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("sin administradores");
    }

    private Actor actor(TipoUsuario rol) {
        return new Actor(99L, "cualquiera", rol);
    }

    private Usuario registrarAdmin(String nombre) {
        return usuarios.guardar(new Usuario(null, nombre, TipoUsuario.ADMIN, "",
                reloj.ahora(), cifrador.cifrar(nombre + "123")));
    }
}
