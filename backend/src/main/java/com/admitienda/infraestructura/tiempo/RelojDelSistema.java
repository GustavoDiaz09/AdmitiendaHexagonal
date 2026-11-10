package com.admitienda.infraestructura.tiempo;

import com.admitienda.dominio.puerto.salida.Reloj;

/**
 * Adaptador de salida: reloj que consulta la hora real del sistema.
 */
public final class RelojDelSistema implements Reloj {

    @Override
    public long ahora() {
        return System.currentTimeMillis();
    }
}
