package com.admitienda.infraestructura.web;

import com.admitienda.dominio.modelo.Actor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Ayuda de los controladores para recuperar el actor que el filtro de
 * sesión dejó en la petición.
 */
final class Actores {

    private Actores() {
    }

    /** Actor autenticado garantizado por {@link FiltroDeSesion}. */
    static Actor de(HttpServletRequest pedido) {
        return (Actor) pedido.getAttribute(FiltroDeSesion.ATRIBUTO_ACTOR);
    }

    /** Token de la sesión garantizado por {@link FiltroDeSesion}. */
    static String tokenDe(HttpServletRequest pedido) {
        return (String) pedido.getAttribute(FiltroDeSesion.ATRIBUTO_TOKEN);
    }
}
