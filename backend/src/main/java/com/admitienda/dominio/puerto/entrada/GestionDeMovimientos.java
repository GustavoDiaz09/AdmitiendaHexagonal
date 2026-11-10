package com.admitienda.dominio.puerto.entrada;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.TipoMovimiento;

import java.util.List;

/**
 * Puerto de entrada: movimientos de caja (ingresos y egresos).
 */
public interface GestionDeMovimientos {

    /**
     * Lista los movimientos entre dos fechas opcionales (en milisegundos
     * epoch) y el saldo resultante de la caja.
     */
    ListadoDeMovimientos listar(Actor actor, Long desde, Long hasta);

    /**
     * Registra un movimiento con la fecha del servidor (requiere rol ADMIN).
     *
     * @throws com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException
     *         si el monto no es positivo o la descripción está vacía.
     */
    Movimiento registrar(Actor actor, TipoMovimiento tipo, Dinero monto, String descripcion);

    /** Elimina un movimiento; el saldo de caja se recalcula (requiere ADMIN). */
    void eliminar(Actor actor, long id);

    /** Listado de movimientos junto con el saldo histórico de la caja. */
    record ListadoDeMovimientos(Dinero saldoCaja, List<Movimiento> movimientos) {
    }
}
