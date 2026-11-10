package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.TipoMovimiento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de movimientos de caja.
 */
public interface RepositorioMovimientos {

    /** Inserta un movimiento nuevo y le asigna identificador. */
    Movimiento guardar(Movimiento movimiento);

    /** Busca un movimiento por identificador. */
    Optional<Movimiento> buscarPorId(long id);

    /**
     * Lista los movimientos entre dos fechas opcionales (milisegundos
     * epoch), del más reciente al más antiguo.
     */
    List<Movimiento> buscarEntre(Long desde, Long hasta);

    /** Total acumulado de un tipo de movimiento sobre toda la historia. */
    Dinero totalDe(TipoMovimiento tipo);

    /** Elimina el movimiento por identificador. */
    void eliminar(long id);
}
