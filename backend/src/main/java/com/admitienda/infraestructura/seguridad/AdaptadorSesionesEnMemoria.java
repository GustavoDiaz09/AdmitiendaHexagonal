package com.admitienda.infraestructura.seguridad;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Usuario;
import com.admitienda.dominio.puerto.salida.Reloj;
import com.admitienda.dominio.puerto.salida.Sesiones;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de salida: sesiones de usuario en memoria con token opaco.
 * La duración se inyecta por configuración; los tokens vencidos se
 * descartan al consultarse. Con sesión en memoria reiniciar el servidor
 * cierra todas las sesiones, lo que para esta tienda es aceptable (los
 * clientes vuelven a entrar).
 */
public final class AdaptadorSesionesEnMemoria implements Sesiones {

    private final Map<String, SesionViva> sesiones = new ConcurrentHashMap<>();
    private final Reloj reloj;
    private final long duracionMilis;
    private final SecureRandom azar = new SecureRandom();

    public AdaptadorSesionesEnMemoria(Reloj reloj, long duracionMilis) {
        this.reloj = reloj;
        this.duracionMilis = duracionMilis;
    }

    @Override
    public String abrir(Usuario usuario) {
        byte[] bytes = new byte[32];
        azar.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        sesiones.put(token, new SesionViva(usuario.id(), usuario.nombreUsuario(),
                usuario.rol(), reloj.ahora() + duracionMilis));
        return token;
    }

    @Override
    public void cerrar(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }

    @Override
    public Optional<Actor> actorDe(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        SesionViva sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (reloj.ahora() >= sesion.vence()) {
            sesiones.remove(token);
            return Optional.empty();
        }
        return Optional.of(new Actor(sesion.usuarioId(), sesion.nombreUsuario(), sesion.rol()));
    }

    private record SesionViva(long usuarioId, String nombreUsuario,
                              com.admitienda.dominio.modelo.TipoUsuario rol, long vence) {
    }
}
