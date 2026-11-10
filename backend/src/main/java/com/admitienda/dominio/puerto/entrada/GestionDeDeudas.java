package com.admitienda.dominio.puerto.entrada;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Deuda;
import com.admitienda.dominio.modelo.Deudor;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.PagoDeuda;

import java.util.List;

/**
 * Puerto de entrada: cartera de fiado (deudores, deudas y abonos).
 */
public interface GestionDeDeudas {

    /** Lista los deudores con el resumen de su cartera (rol REGISTRADO o superior). */
    List<ResumenDeDeudor> listarDeudores(Actor actor);

    /**
     * Historial completo de un deudor: sus deudas y, dentro de cada una,
     * los abonos recibidos.
     *
     * @throws com.admitienda.dominio.modelo.excepciones.RecursoNoEncontradoException
     *         si el deudor no existe.
     */
    HistorialDeDeudor historial(Actor actor, long idDeudor);

    /**
     * Registra una venta fiada resolviendo el deudor por nombre
     * normalizado: si no existe se crea (requiere rol ADMIN).
     */
    Deuda registrar(Actor actor, String nombreDeudor, Dinero monto, String descripcion);

    /**
     * Edita el valor y la descripción de una deuda conservando lo abonado
     * (requiere rol ADMIN).
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si el nuevo monto es menor que lo ya abonado.
     */
    Deuda editar(Actor actor, long idDeuda, Dinero monto, String descripcion);

    /**
     * Abona a una deuda: descuenta el saldo y registra un INGRESO en caja
     * por el mismo valor, de forma atómica (requiere rol ADMIN).
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si la deuda está saldada o el abono supera el saldo.
     */
    Deuda abonar(Actor actor, long idDeuda, Dinero monto, String descripcion);

    /** Elimina una deuda junto con sus abonos (requiere rol ADMIN). */
    void eliminar(Actor actor, long idDeuda);

    /** Deudor con el estado de su cartera. */
    record ResumenDeDeudor(Deudor deudor, int deudasPendientes, Dinero totalPendiente,
                           long fechaUltimaDeuda) {
    }

    /** Una deuda junto con sus abonos. */
    record DeudaConPagos(Deuda deuda, List<PagoDeuda> pagos) {
    }

    /** Historial completo de un deudor. */
    record HistorialDeDeudor(Deudor deudor, List<DeudaConPagos> deudas) {
    }
}
