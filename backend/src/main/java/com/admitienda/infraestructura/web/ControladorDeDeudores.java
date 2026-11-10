package com.admitienda.infraestructura.web;

import com.admitienda.dominio.puerto.entrada.GestionDeDeudas;
import com.admitienda.infraestructura.web.dto.DeudorResumenDto;
import com.admitienda.infraestructura.web.dto.HistorialDeDeudorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP para la consulta de deudores.
 */
@RestController
@RequestMapping("/api/deudores")
public final class ControladorDeDeudores {

    private final GestionDeDeudas deudas;

    public ControladorDeDeudores(GestionDeDeudas deudas) {
        this.deudas = deudas;
    }

    @GetMapping
    public List<DeudorResumenDto> listar(HttpServletRequest pedido) {
        return deudas.listarDeudores(Actores.de(pedido)).stream()
                .map(DeudorResumenDto::desde).toList();
    }

    @GetMapping("/{id}")
    public HistorialDeDeudorDto historial(@PathVariable long id, HttpServletRequest pedido) {
        return HistorialDeDeudorDto.desde(deudas.historial(Actores.de(pedido), id));
    }
}
