package com.admitienda.aplicacion;

import com.admitienda.dominio.compartido.Validaciones;
import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Deuda;
import com.admitienda.dominio.modelo.Deudor;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.PagoDeuda;
import com.admitienda.dominio.modelo.TipoMovimiento;
import com.admitienda.dominio.modelo.excepciones.RecursoNoEncontradoException;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeDeudas;
import com.admitienda.dominio.puerto.salida.RepositorioDeudas;
import com.admitienda.dominio.puerto.salida.RepositorioDeudores;
import com.admitienda.dominio.puerto.salida.RepositorioMovimientos;
import com.admitienda.dominio.puerto.salida.RepositorioPagosDeuda;
import com.admitienda.dominio.puerto.salida.Reloj;
import com.admitienda.dominio.puerto.salida.UnidadDeTrabajo;

import java.util.List;

/**
 * Caso de uso: cartera de fiado. Mantiene las reglas originales del
 * sistema: deudor único por nombre normalizado, varias deudas por deudor,
 * saldo que decrece con abonos y un ingreso en caja por cada abono.
 */
public final class CasosDeUsoDeudas implements GestionDeDeudas {

    private final RepositorioDeudores deudores;
    private final RepositorioDeudas deudas;
    private final RepositorioPagosDeuda pagos;
    private final RepositorioMovimientos movimientos;
    private final Reloj reloj;
    private final UnidadDeTrabajo unidadDeTrabajo;

    public CasosDeUsoDeudas(RepositorioDeudores deudores, RepositorioDeudas deudas,
                            RepositorioPagosDeuda pagos, RepositorioMovimientos movimientos,
                            Reloj reloj, UnidadDeTrabajo unidadDeTrabajo) {
        this.deudores = deudores;
        this.deudas = deudas;
        this.pagos = pagos;
        this.movimientos = movimientos;
        this.reloj = reloj;
        this.unidadDeTrabajo = unidadDeTrabajo;
    }

    @Override
    public List<ResumenDeDeudor> listarDeudores(Actor actor) {
        ReglasDeAcceso.exigirRegistrado(actor, "ver la cartera");
        return deudores.listarTodos().stream()
                .map(this::resumirCartera)
                .toList();
    }

    @Override
    public HistorialDeDeudor historial(Actor actor, long idDeudor) {
        ReglasDeAcceso.exigirRegistrado(actor, "ver la cartera");
        Deudor deudor = deudores.buscarPorId(idDeudor)
                .orElseThrow(() -> new RecursoNoEncontradoException("El deudor no existe."));
        List<DeudaConPagos> historial = deudas.listarPorDeudor(idDeudor).stream()
                .map(deuda -> new DeudaConPagos(deuda, pagos.listarPorDeuda(deuda.id())))
                .toList();
        return new HistorialDeDeudor(deudor, historial);
    }

    @Override
    public Deuda registrar(Actor actor, String nombreDeudor, Dinero monto, String descripcion) {
        ReglasDeAcceso.exigirAdministrador(actor, "registrar deudas");
        String nombre = Validaciones.requerirTexto(nombreDeudor, "nombre del deudor");
        String descripcionLimpia = Validaciones.requerirTexto(descripcion, "descripción");
        if (!monto.esPositivo()) {
            throw new ReglaDeNegocioException("El monto de la deuda debe ser mayor a 0.");
        }
        String normalizado = Validaciones.normalizarNombre(nombre);
        String nombreLimpio = nombre.trim().replaceAll("\\s+", " ");
        return unidadDeTrabajo.ejecutar(() -> {
            Deudor deudor = deudores.buscarPorNombreNormalizado(normalizado)
                    .orElseGet(() -> deudores.guardar(
                            new Deudor(null, nombreLimpio, normalizado)));
            Deuda nueva = new Deuda(null, deudor.id(), deudor.nombreDeudor(), monto, monto,
                    descripcionLimpia, reloj.ahora());
            return deudas.guardar(nueva);
        });
    }

    @Override
    public Deuda editar(Actor actor, long idDeuda, Dinero monto, String descripcion) {
        ReglasDeAcceso.exigirAdministrador(actor, "editar deudas");
        Deuda deuda = deudas.buscarPorId(idDeuda).orElseThrow(
                () -> new RecursoNoEncontradoException("La deuda no existe o ya fue eliminada."));
        String descripcionLimpia = Validaciones.requerirTexto(descripcion, "descripción");
        if (!monto.esPositivo()) {
            throw new ReglaDeNegocioException("El monto de la deuda debe ser mayor a 0.");
        }
        Dinero abonado = deuda.abonado();
        if (monto.esMenorQue(abonado)) {
            throw new ReglaDeNegocioException(
                    "El monto no puede ser menor que lo ya abonado (%s).".formatted(abonado));
        }
        return deudas.actualizar(deuda.conDetalle(monto, monto.restar(abonado), descripcionLimpia));
    }

    @Override
    public Deuda abonar(Actor actor, long idDeuda, Dinero monto, String descripcion) {
        ReglasDeAcceso.exigirAdministrador(actor, "registrar abonos");
        if (!monto.esPositivo()) {
            throw new ReglaDeNegocioException("El abono debe ser mayor a 0.");
        }
        Deuda deuda = deudas.buscarPorId(idDeuda).orElseThrow(
                () -> new RecursoNoEncontradoException("La deuda no existe o ya fue eliminada."));
        if (deuda.estaSaldada()) {
            throw new ReglaDeNegocioException("Esta deuda ya está saldada.");
        }
        if (monto.esMayorQue(deuda.saldo())) {
            throw new ReglaDeNegocioException(
                    "El abono supera el saldo pendiente (%s).".formatted(deuda.saldo()));
        }
        String descripcionLimpia = descripcion == null ? "" : descripcion.trim();
        String detalleDelPago = descripcionLimpia.isEmpty() ? "Abono a cuenta" : descripcionLimpia;
        return unidadDeTrabajo.ejecutar(() -> {
            pagos.guardar(new PagoDeuda(null, deuda.id(), monto, detalleDelPago, reloj.ahora()));
            Deuda actualizada = deudas.actualizar(
                    deuda.conSaldo(deuda.saldo().restar(monto)));
            String concepto = "Pago de deuda de " + deuda.clienteNombre()
                    + (descripcionLimpia.isEmpty() ? "" : ": " + descripcionLimpia);
            movimientos.guardar(new Movimiento(null, TipoMovimiento.INGRESO, monto,
                    concepto, reloj.ahora()));
            return actualizada;
        });
    }

    @Override
    public void eliminar(Actor actor, long idDeuda) {
        ReglasDeAcceso.exigirAdministrador(actor, "eliminar deudas");
        deudas.buscarPorId(idDeuda).orElseThrow(
                () -> new RecursoNoEncontradoException("La deuda no existe o ya fue eliminada."));
        unidadDeTrabajo.ejecutar(() -> {
            pagos.eliminarPorDeuda(idDeuda);
            deudas.eliminar(idDeuda);
        });
    }

    /** Calcula el estado de la cartera de un deudor. */
    private ResumenDeDeudor resumirCartera(Deudor deudor) {
        List<Deuda> cartera = deudas.listarPorDeudor(deudor.id());
        int pendientes = 0;
        Dinero totalPendiente = Dinero.cero();
        long fechaUltima = 0;
        for (Deuda deuda : cartera) {
            if (!deuda.estaSaldada()) {
                pendientes++;
                totalPendiente = totalPendiente.sumar(deuda.saldo());
            }
            fechaUltima = Math.max(fechaUltima, deuda.fecha());
        }
        return new ResumenDeDeudor(deudor, pendientes, totalPendiente, fechaUltima);
    }
}
