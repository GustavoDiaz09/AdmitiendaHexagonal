package com.admitienda.infraestructura.web;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.puerto.salida.Sesiones;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Adaptador de entrada: traduce la cabecera HTTP "Authorization: Bearer"
 * al actor autenticado y lo publica como atributo de la petición. Las
 * rutas públicas (iniciar sesión, registrarse y consultar el catálogo)
 * siguen de largo sin sesión.
 */
public final class FiltroDeSesion implements Filter {

    /** Atributo de petición con el actor autenticado. */
    public static final String ATRIBUTO_ACTOR = "actor";

    /** Atributo de petición con el token de la sesión (para cerrarla). */
    public static final String ATRIBUTO_TOKEN = "tokenDeSesion";

    private final Sesiones sesiones;

    public FiltroDeSesion(Sesiones sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public void doFilter(ServletRequest pedido, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) pedido;
        HttpServletResponse respuestaHttp = (HttpServletResponse) respuesta;

        if (esRutaPublica(http.getMethod(), http.getRequestURI())) {
            cadena.doFilter(pedido, respuesta);
            return;
        }

        String token = tokenDe(http);
        var actor = sesiones.actorDe(token);
        if (actor.isEmpty()) {
            responderSinSesion(respuestaHttp);
            return;
        }
        http.setAttribute(ATRIBUTO_ACTOR, actor.get());
        http.setAttribute(ATRIBUTO_TOKEN, token);
        cadena.doFilter(pedido, respuesta);
    }

    private String tokenDe(HttpServletRequest pedido) {
        String cabecera = pedido.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            return null;
        }
        return cabecera.substring("Bearer ".length()).trim();
    }

    private boolean esRutaPublica(String metodo, String ruta) {
        return "POST".equals(metodo) && ruta.equals("/api/sesiones")
                || "POST".equals(metodo) && ruta.equals("/api/usuarios")
                || "GET".equals(metodo) && ruta.startsWith("/api/productos");
    }

    private void responderSinSesion(HttpServletResponse respuesta) throws IOException {
        respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("{\"error\": \"Debes iniciar sesión para usar la tienda.\"}");
    }
}
