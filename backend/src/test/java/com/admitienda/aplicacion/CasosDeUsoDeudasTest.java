package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Deuda;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.TipoMovimiento;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeDeudas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del caso de uso de deudas: el flujo fiado → abono → ingreso
 * en caja y las reglas que lo protegen.
 */
class CasosDeUsoDeudasTest {

    private final Falsos.DeudoresEnMemoria deudores = new Falsos.DeudoresEnMemoria();
    private final Falsos.DeudasEnMemoria deudas = new Falsos.DeudasEnMemoria();
    private final Falsos.PagosEnMemoria pagos = new Falsos.PagosEnMemoria();
    private final Falsos.MovimientosEnMemoria movimientos = new Falsos.MovimientosEnMemoria();
    private final Falsos.RelojFijo reloj = new Falsos.RelojFijo();

    private GestionDeDeudas casos;
    private final Actor admin = new Actor(1L, "admin", TipoUsuario.ADMIN);

    @BeforeEach
    void preparar() {
        casos = new CasosDeUsoDeudas(deudores, deudas, pagos, movimientos, reloj,
                new Falsos.UnidadDirecta());
    }

    @Test
    void registrarCreaDeudorSiNoExisteYReutilizaPorNombreNormalizado() {
        Deuda primera = casos.registrar(admin, "María Pérez",
                Dinero.dePesos(50_000, "monto"), "Mercado");
        Deuda segunda = casos.registrar(admin, "  MARÍA   pérez ",
                Dinero.dePesos(10_000, "monto"), "Otro mercado");

        assertThat(deudores.listarTodos()).hasSize(1);
        assertThat(primera.deudorId()).isEqualTo(segunda.deudorId());
        assertThat(deudores.listarTodos().get(0).nombreDeudor()).isEqualTo("María Pérez");
    }

    @Test
    void registrarExigeAdministrador() {
        Actor registrado = new Actor(2L, "ana", TipoUsuario.REGISTRADO);

        assertThatThrownBy(() -> casos.registrar(registrado, "Pedro",
                Dinero.dePesos(1000, "monto"), "x"))
                .isInstanceOf(com.admitienda.dominio.modelo.excepciones.PermisoDenegadoException.class);
    }

    @Test
    void abonarDescuentaElSaldoYGeneraIngresoEnCaja() {
        Deuda deuda = casos.registrar(admin, "María Pérez",
                Dinero.dePesos(50_000, "monto"), "Mercado");

        Deuda abonada = casos.abonar(admin, deuda.id(), Dinero.dePesos(20_000, "monto"), "Abono");

        assertThat(abonada.saldo()).isEqualTo(Dinero.dePesos(30_000, "monto"));
        assertThat(pagos.listarPorDeuda(deuda.id())).hasSize(1);
        assertThat(movimientos.tabla).hasSize(1);
        Movimiento ingreso = movimientos.tabla.get(0);
        assertThat(ingreso.tipo()).isEqualTo(TipoMovimiento.INGRESO);
        assertThat(ingreso.monto()).isEqualTo(Dinero.dePesos(20_000, "monto"));
        assertThat(ingreso.descripcion()).contains("María Pérez").contains("Abono");
    }

    @Test
    void abonarMasQueElSaldoEstaProhibido() {
        Deuda deuda = casos.registrar(admin, "María",
                Dinero.dePesos(5000, "monto"), "Mercado");

        assertThatThrownBy(() -> casos.abonar(admin, deuda.id(),
                Dinero.dePesos(5001, "monto"), "x"))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("supera el saldo");
    }

    @Test
    void abonarDeudaSaldadaEstaProhibido() {
        Deuda deuda = casos.registrar(admin, "María",
                Dinero.dePesos(5000, "monto"), "Mercado");
        casos.abonar(admin, deuda.id(), Dinero.dePesos(5000, "monto"), "Total");

        assertThatThrownBy(() -> casos.abonar(admin, deuda.id(),
                Dinero.dePesos(1, "monto"), "x"))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("saldada");
    }

    @Test
    void editarNoPermiteReducirElMontoPorDebajoDeLoAbonado() {
        Deuda deuda = casos.registrar(admin, "María",
                Dinero.dePesos(10_000, "monto"), "Mercado");
        casos.abonar(admin, deuda.id(), Dinero.dePesos(4_000, "monto"), "Abono");

        assertThatThrownBy(() -> casos.editar(admin, deuda.id(),
                Dinero.dePesos(3_999, "monto"), "x"))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("menor que lo ya abonado");

        Deuda editada = casos.editar(admin, deuda.id(), Dinero.dePesos(12_000, "monto"), "Mercado ampliado");
        assertThat(editada.monto()).isEqualTo(Dinero.dePesos(12_000, "monto"));
        assertThat(editada.saldo()).isEqualTo(Dinero.dePesos(8_000, "monto"));
    }

    @Test
    void listarDeudoresResumeLaCartera() {
        casos.registrar(admin, "María", Dinero.dePesos(10_000, "monto"), "A");
        casos.registrar(admin, "Pedro", Dinero.dePesos(5_000, "monto"), "B");
        casos.abonar(admin, 2L, Dinero.dePesos(5_000, "monto"), "Saldada");

        var resumen = casos.listarDeudores(new Actor(3L, "ana", TipoUsuario.REGISTRADO));

        assertThat(resumen).hasSize(2);
        var maria = resumen.stream()
                .filter(r -> r.deudor().nombreDeudor().equals("María")).findFirst().orElseThrow();
        assertThat(maria.deudasPendientes()).isEqualTo(1);
        assertThat(maria.totalPendiente()).isEqualTo(Dinero.dePesos(10_000, "monto"));
    }

    @Test
    void eliminarBorraLaDeudaYSusPagosPeroNoElIngresoDeCaja() {
        Deuda deuda = casos.registrar(admin, "María",
                Dinero.dePesos(5000, "monto"), "Mercado");
        casos.abonar(admin, deuda.id(), Dinero.dePesos(2000, "monto"), "Abono");

        casos.eliminar(admin, deuda.id());

        assertThat(deudas.buscarPorId(deuda.id())).isEmpty();
        assertThat(pagos.listarPorDeuda(deuda.id())).isEmpty();
        assertThat(movimientos.tabla).hasSize(1);
    }
}
