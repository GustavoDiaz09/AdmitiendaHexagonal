package com.admitienda.infraestructura.web;

import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.TipoMovimiento;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeMovimientos;
import com.admitienda.infraestructura.web.dto.ListadoDeMovimientosDto;
import com.admitienda.infraestructura.web.dto.MovimientoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Adaptador de entrada HTTP para la caja (ingresos y egresos).
 */
@RestController
@RequestMapping("/api/movimientos")
public final class ControladorDeMovimientos {

    private final GestionDeMovimientos movimientos;

    public ControladorDeMovimientos(GestionDeMovimientos movimientos) {
        this.movimientos = movimientos;
    }

    /** Cuerpo de la petición de registro de movimiento. */
    record PeticionDeMovimiento(String tipo, double monto, String descripcion) {
    }

    @GetMapping
    public ListadoDeMovimientosDto listar(
            @RequestParam(required = false) Long desde,
            @RequestParam(required = false) Long hasta,
            HttpServletRequest pedido) {
        return ListadoDeMovimientosDto.desde(
                movimientos.listar(Actores.de(pedido), desde, hasta));
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public MovimientoDto registrar(@RequestBody PeticionDeMovimiento peticion,
                                   HttpServletRequest pedido) {
        return MovimientoDto.desde(movimientos.registrar(Actores.de(pedido),
                tipoDesde(peticion.tipo()),
                Dinero.dePesos(peticion.monto(), "monto"),
                peticion.descripcion()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void eliminar(@PathVariable long id, HttpServletRequest pedido) {
        movimientos.eliminar(Actores.de(pedido), id);
    }

    private TipoMovimiento tipoDesde(String tipo) {
        try {
            return TipoMovimiento.valueOf(tipo);
        } catch (IllegalArgumentException e) {
            throw new ReglaDeNegocioException("El tipo debe ser INGRESO o EGRESO.");
        }
    }
}
