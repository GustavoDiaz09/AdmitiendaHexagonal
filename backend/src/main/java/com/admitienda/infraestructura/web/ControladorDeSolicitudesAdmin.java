package com.admitienda.infraestructura.web;

import com.admitienda.dominio.puerto.entrada.GestionDeSolicitudesAdmin;
import com.admitienda.infraestructura.web.dto.SolicitudDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * Adaptador de entrada HTTP para las solicitudes de permiso de
 * administrador.
 */
@RestController
@RequestMapping("/api/solicitudes-admin")
public final class ControladorDeSolicitudesAdmin {

    private final GestionDeSolicitudesAdmin solicitudes;

    public ControladorDeSolicitudesAdmin(GestionDeSolicitudesAdmin solicitudes) {
        this.solicitudes = solicitudes;
    }

    /** Cuerpo de la petición de solicitud de permiso. */
    record PeticionDeMotivo(String motivo) {
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public SolicitudDto solicitar(@RequestBody PeticionDeMotivo peticion,
                                   HttpServletRequest pedido) {
        return SolicitudDto.desde(
                solicitudes.solicitar(Actores.de(pedido), peticion.motivo()));
    }

    @GetMapping
    public List<SolicitudDto> listar(HttpServletRequest pedido) {
        return solicitudes.listar(Actores.de(pedido)).stream()
                .map(SolicitudDto::desde).toList();
    }

    @PostMapping("/{id}/aprobar")
    public SolicitudDto aprobar(@PathVariable long id, HttpServletRequest pedido) {
        return SolicitudDto.desde(solicitudes.aprobar(Actores.de(pedido), id));
    }

    @PostMapping("/{id}/rechazar")
    public SolicitudDto rechazar(@PathVariable long id, HttpServletRequest pedido) {
        return SolicitudDto.desde(solicitudes.rechazar(Actores.de(pedido), id));
    }
}
