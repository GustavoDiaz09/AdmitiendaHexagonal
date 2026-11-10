package com.admitienda.infraestructura.web;

import com.admitienda.dominio.puerto.entrada.ConsultaDeResumenes;
import com.admitienda.infraestructura.web.dto.ResumenDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada HTTP para el resumen financiero de la tienda.
 */
@RestController
@RequestMapping("/api/resumenes")
public final class ControladorDeResumenes {

    private final ConsultaDeResumenes resumenes;

    public ControladorDeResumenes(ConsultaDeResumenes resumenes) {
        this.resumenes = resumenes;
    }

    @GetMapping
    public ResumenDto resumen(HttpServletRequest pedido) {
        return ResumenDto.desde(resumenes.obtenerResumen(Actores.de(pedido)));
    }
}
