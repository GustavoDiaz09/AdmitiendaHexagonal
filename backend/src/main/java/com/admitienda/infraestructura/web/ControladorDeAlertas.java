package com.admitienda.infraestructura.web;

import com.admitienda.dominio.puerto.entrada.ConsultaDeAlertas;
import com.admitienda.infraestructura.web.dto.AlertaDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP para las alertas de inventario bajo.
 */
@RestController
@RequestMapping("/api/alertas")
public final class ControladorDeAlertas {

    private final ConsultaDeAlertas alertas;

    public ControladorDeAlertas(ConsultaDeAlertas alertas) {
        this.alertas = alertas;
    }

    @GetMapping
    public List<AlertaDto> alertas(HttpServletRequest pedido) {
        return alertas.productosConStockBajo(Actores.de(pedido)).stream()
                .map(AlertaDto::desde).toList();
    }
}
