package com.admitienda.aplicacion;

import com.admitienda.dominio.compartido.Validaciones;
import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.TipoMovimiento;
import com.admitienda.dominio.modelo.excepciones.RecursoNoEncontradoException;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeMovimientos;
import com.admitienda.dominio.puerto.salida.RepositorioMovimientos;
import com.admitienda.dominio.puerto.salida.Reloj;

import java.util.List;

/**
 * Caso de uso: movimientos de caja (ingresos y egresos).
 */
public final class CasosDeUsoMovimientos implements GestionDeMovimientos {

    private final RepositorioMovimientos repositorio;
    private final Reloj reloj;

    public CasosDeUsoMovimientos(RepositorioMovimientos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Override
    public ListadoDeMovimientos listar(Actor actor, Long desde, Long hasta) {
        ReglasDeAcceso.exigirRegistrado(actor, "ver los movimientos");
        Dinero saldoCaja = repositorio.totalDe(TipoMovimiento.INGRESO)
                .restar(repositorio.totalDe(TipoMovimiento.EGRESO));
        return new ListadoDeMovimientos(saldoCaja, repositorio.buscarEntre(desde, hasta));
    }

    @Override
    public Movimiento registrar(Actor actor, TipoMovimiento tipo, Dinero monto,
                                 String descripcion) {
        ReglasDeAcceso.exigirAdministrador(actor, "registrar movimientos");
        String descripcionLimpia = Validaciones.requerirTexto(descripcion, "descripción");
        if (!monto.esPositivo()) {
            throw new ReglaDeNegocioException("El monto del movimiento debe ser mayor a 0.");
        }
        Movimiento nuevo = new Movimiento(null, tipo, monto, descripcionLimpia, reloj.ahora());
        return repositorio.guardar(nuevo);
    }

    @Override
    public void eliminar(Actor actor, long id) {
        ReglasDeAcceso.exigirAdministrador(actor, "eliminar movimientos");
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El movimiento no existe."));
        repositorio.eliminar(id);
    }
}
