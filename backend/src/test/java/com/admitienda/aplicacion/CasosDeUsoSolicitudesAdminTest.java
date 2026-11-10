package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.Usuario;
import com.admitienda.dominio.puerto.entrada.GestionDeSolicitudesAdmin;
import com.admitienda.dominio.puerto.entrada.GestionDeUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del flujo de solicitudes de permiso de administrador.
 */
class CasosDeUsoSolicitudesAdminTest {

    private final Falsos.UsuariosEnMemoria usuarios = new Falsos.UsuariosEnMemoria();
    private final Falsos.SolicitudesEnMemoria solicitudes = new Falsos.SolicitudesEnMemoria();
    private final Falsos.CifradorFalso cifrador = new Falsos.CifradorFalso();
    private final Falsos.RelojFijo reloj = new Falsos.RelojFijo();

    private GestionDeSolicitudesAdmin casos;
    private GestionDeUsuarios gestionDeUsuarios;
    private Actor ana;
    private Actor admin;

    @BeforeEach
    void preparar() {
        gestionDeUsuarios = new CasosDeUsoUsuarios(usuarios, solicitudes, cifrador, reloj,
                new Falsos.UnidadDirecta(), new Falsos.SesionesEnMemoria());
        casos = new CasosDeUsoSolicitudesAdmin(solicitudes, usuarios, reloj,
                new Falsos.UnidadDirecta());

        Usuario cuentaAna = gestionDeUsuarios.registrar(
                new GestionDeUsuarios.RegistroDeUsuario("ana", "ana12345", null));
        ana = new Actor(cuentaAna.id(), "ana", TipoUsuario.REGISTRADO);
        Usuario cuentaAdmin = usuarios.guardar(new Usuario(null, "admin", TipoUsuario.ADMIN,
                "", reloj.ahora(), cifrador.cifrar("admin123")));
        admin = new Actor(cuentaAdmin.id(), "admin", TipoUsuario.ADMIN);
    }

    @Test
    void unRegistradoPuedeSolicitarElPermiso() {
        var solicitud = casos.solicitar(ana, "Ayudo los domingos");

        assertThat(solicitud.estado()).isEqualTo(
                com.admitienda.dominio.modelo.EstadoSolicitud.PENDIENTE);
        assertThat(solicitud.nombreUsuario()).isEqualTo("ana");
    }

    @Test
    void unAdministradorNoNecesitaSolicitar() {
        assertThatThrownBy(() -> casos.solicitar(admin, "ya soy admin"))
                .isInstanceOf(com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException.class)
                .hasMessageContaining("no necesitan solicitar");
    }

    @Test
    void soloSePermiteUnaSolicitudPendiente() {
        casos.solicitar(ana, "primera");

        assertThatThrownBy(() -> casos.solicitar(ana, "segunda"))
                .isInstanceOf(com.admitienda.dominio.modelo.excepciones.ConflictoDeDatosException.class)
                .hasMessageContaining("pendiente");
    }

    @Test
    void aprobarPromueveAlSolicitanteYCierraLaSolicitud() {
        var solicitud = casos.solicitar(ana, "ayudo");

        casos.aprobar(admin, solicitud.id());

        assertThat(usuarios.buscarPorId(ana.id()).orElseThrow().esAdministrador()).isTrue();
        assertThat(solicitudes.buscarPorId(solicitud.id()).orElseThrow().estado())
                .isEqualTo(com.admitienda.dominio.modelo.EstadoSolicitud.APROBADA);
    }

    @Test
    void rechazarCambiaElEstadoSinPromover() {
        var solicitud = casos.solicitar(ana, "ayudo");

        casos.rechazar(admin, solicitud.id());

        assertThat(usuarios.buscarPorId(ana.id()).orElseThrow().esAdministrador()).isFalse();
        assertThat(solicitudes.buscarPorId(solicitud.id()).orElseThrow().estado())
                .isEqualTo(com.admitienda.dominio.modelo.EstadoSolicitud.RECHAZADA);
    }

    @Test
    void unaSolicitudResueltaNoSeVuelveADecidir() {
        var solicitud = casos.solicitar(ana, "ayudo");
        casos.aprobar(admin, solicitud.id());

        assertThatThrownBy(() -> casos.aprobar(admin, solicitud.id()))
                .isInstanceOf(com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException.class)
                .hasMessageContaining("resuelta");
    }

    @Test
    void soloElAdministradorListaYDecide() {
        assertThatThrownBy(() -> casos.listar(ana))
                .isInstanceOf(com.admitienda.dominio.modelo.excepciones.PermisoDenegadoException.class);
        assertThatThrownBy(() -> casos.aprobar(ana, 1L))
                .isInstanceOf(com.admitienda.dominio.modelo.excepciones.PermisoDenegadoException.class);
    }
}
