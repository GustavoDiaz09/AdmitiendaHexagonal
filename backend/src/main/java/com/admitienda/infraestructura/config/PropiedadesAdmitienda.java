package com.admitienda.infraestructura.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Propiedades de configuración de Admitienda (prefijo {@code admitienda}).
 *
 * @param corsOrigenes       orígenes permitidos por CORS (el frontend Vite).
 * @param duracionSesionHoras vigencia de una sesión iniciada.
 * @param adminInicial       credenciales del administrador que se crea la
 *                           primera vez que arranca la base vacía.
 */
@ConfigurationProperties(prefix = "admitienda")
public record PropiedadesAdmitienda(List<String> corsOrigenes,
                                    long duracionSesionHoras,
                                    AdminInicial adminInicial) {

    public PropiedadesAdmitienda {
        corsOrigenes = corsOrigenes == null ? new ArrayList<>() : corsOrigenes;
    }

    /**
     * Cuenta administrativa de arranque.
     *
     * @param nombreUsuario nombre de la cuenta.
     * @param contrasena    contraseña en texto claro (solo se usa para
     *                      sembrar el hash; nunca se persiste).
     */
    public record AdminInicial(String nombreUsuario, String contrasena) {
    }
}
