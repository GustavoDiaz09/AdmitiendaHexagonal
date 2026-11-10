package com.admitienda.infraestructura.web;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Deuda;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.puerto.entrada.GestionDeDeudas;
import com.admitienda.infraestructura.web.dto.DeudaDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Adaptador de entrada HTTP para las deudas (ventas fiadas y abonos).
 */
@RestController
@RequestMapping("/api/deudas")
public final class ControladorDeDeudas {

    private final GestionDeDeudas deudas;

    public ControladorDeDeudas(GestionDeDeudas deudas) {
        this.deudas = deudas;
    }

    /** Cuerpo de la petición de registro de venta fiada. */
    record PeticionDeDeuda(String nombreDeudor, double monto, String descripcion) {
    }

    /** Cuerpo de la petición de edición de deuda. */
    record PeticionDeEdicion(double monto, String descripcion) {
    }

    /** Cuerpo de la petición de abono. */
    record PeticionDeAbono(double monto, String descripcion) {
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public DeudaDto registrar(@RequestBody PeticionDeDeuda peticion,
                              HttpServletRequest pedido) {
        var deuda = deudas.registrar(Actores.de(pedido), peticion.nombreDeudor(),
                Dinero.dePesos(peticion.monto(), "monto"), peticion.descripcion());
        return DeudaDto.desde(new GestionDeDeudas.DeudaConPagos(deuda, List.of()));
    }

    @PutMapping("/{id}")
    public DeudaDto editar(@PathVariable long id, @RequestBody PeticionDeEdicion peticion,
                           HttpServletRequest pedido) {
        var deuda = deudas.editar(Actores.de(pedido), id,
                Dinero.dePesos(peticion.monto(), "monto"), peticion.descripcion());
        return conPagos(Actores.de(pedido), deuda);
    }

    @PostMapping("/{id}/abonos")
    public DeudaDto abonar(@PathVariable long id, @RequestBody PeticionDeAbono peticion,
                           HttpServletRequest pedido) {
        var deuda = deudas.abonar(Actores.de(pedido), id,
                Dinero.dePesos(peticion.monto(), "monto"), peticion.descripcion());
        return conPagos(Actores.de(pedido), deuda);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void eliminar(@PathVariable long id, HttpServletRequest pedido) {
        deudas.eliminar(Actores.de(pedido), id);
    }

    /** Recupera los abonos de la deuda desde el historial del deudor. */
    private DeudaDto conPagos(Actor actor, Deuda deuda) {
        return deudas.historial(actor, deuda.deudorId()).deudas().stream()
                .filter(par -> par.deuda().id() == deuda.id())
                .findFirst()
                .map(DeudaDto::desde)
                .orElseGet(() -> DeudaDto.desde(
                        new GestionDeDeudas.DeudaConPagos(deuda, List.of())));
    }
}
