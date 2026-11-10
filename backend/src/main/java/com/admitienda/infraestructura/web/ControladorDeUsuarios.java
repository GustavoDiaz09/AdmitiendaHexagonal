package com.admitienda.infraestructura.web;

import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import com.admitienda.dominio.puerto.entrada.GestionDeUsuarios;
import com.admitienda.infraestructura.web.dto.UsuarioDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
 * Adaptador de entrada HTTP para usuarios: registro y administración
 * de cuentas.
 */
@RestController
@RequestMapping("/api/usuarios")
public final class ControladorDeUsuarios {

    private final GestionDeUsuarios usuarios;

    public ControladorDeUsuarios(GestionDeUsuarios usuarios) {
        this.usuarios = usuarios;
    }

    /** Cuerpo de la petición de registro. */
    record PeticionDeRegistro(String nombreUsuario, String contrasena, String indicio) {
    }

    /** Cuerpo de la petición de cambio de rol. */
    record PeticionDeRol(String rol) {
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public UsuarioDto registrar(@RequestBody PeticionDeRegistro peticion) {
        return UsuarioDto.desde(usuarios.registrar(new GestionDeUsuarios.RegistroDeUsuario(
                peticion.nombreUsuario(), peticion.contrasena(), peticion.indicio())));
    }

    @GetMapping
    public List<UsuarioDto> listar(HttpServletRequest pedido) {
        return usuarios.listar(Actores.de(pedido)).stream().map(UsuarioDto::desde).toList();
    }

    @PutMapping("/{id}/rol")
    public UsuarioDto cambiarRol(@PathVariable long id,
                                 @RequestBody PeticionDeRol peticion,
                                 HttpServletRequest pedido) {
        return UsuarioDto.desde(usuarios.cambiarRol(
                Actores.de(pedido), id, rolDesde(peticion.rol())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void eliminar(@PathVariable long id, HttpServletRequest pedido) {
        usuarios.eliminar(Actores.de(pedido), id);
    }

    private TipoUsuario rolDesde(String rol) {
        try {
            return TipoUsuario.valueOf(rol);
        } catch (IllegalArgumentException e) {
            throw new ReglaDeNegocioException("El rol debe ser INVITADO, REGISTRADO o ADMIN.");
        }
    }
}
