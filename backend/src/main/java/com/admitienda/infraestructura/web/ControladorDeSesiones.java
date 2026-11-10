package com.admitienda.infraestructura.web;

import com.admitienda.dominio.puerto.entrada.GestionDeSesiones;
import com.admitienda.infraestructura.web.dto.SesionActualDto;
import com.admitienda.infraestructura.web.dto.SesionDto;
import com.admitienda.infraestructura.web.dto.UsuarioActualDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Adaptador de entrada HTTP para sesiones: iniciar, cerrar y validar.
 */
@RestController
@RequestMapping("/api/sesiones")
public final class ControladorDeSesiones {

    private final GestionDeSesiones sesiones;

    public ControladorDeSesiones(GestionDeSesiones sesiones) {
        this.sesiones = sesiones;
    }

    /** Cuerpo de la petición de inicio de sesión. */
    record PeticionDeSesion(String nombreUsuario, String contrasena) {
    }

    @PostMapping
    public SesionDto iniciar(@RequestBody PeticionDeSesion peticion) {
        return SesionDto.desde(sesiones.iniciarSesion(
                peticion.nombreUsuario(), peticion.contrasena()));
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public void cerrar(HttpServletRequest pedido) {
        sesiones.cerrarSesion(Actores.tokenDe(pedido));
    }

    @GetMapping("/actual")
    public SesionActualDto actual(HttpServletRequest pedido) {
        return new SesionActualDto(UsuarioActualDto.desde(Actores.de(pedido)));
    }
}
