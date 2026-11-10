package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.excepciones.PermisoDenegadoException;

/**
 * Reglas de acceso compartidas por los casos de uso.
 *
 * <p>Nació de un refactoring: la verificación de rol estaba duplicada
 * dentro de cada caso de uso y se centralizó aquí (DRY), de modo que un
 * cambio en la política de acceso se hace en un solo lugar.</p>
 */
public final class ReglasDeAcceso {

    private ReglasDeAcceso() {
    }

    /**
     * Exige que el actor tenga rol ADMIN.
     *
     * @param accion  descripción de la acción, para el mensaje de error
     *               ("Solo un administrador puede &lt;accion&gt;.").
     */
    public static void exigirAdministrador(Actor actor, String accion) {
        if (!actor.esAdministrador()) {
            throw new PermisoDenegadoException(
                    "Solo un administrador puede %s.".formatted(accion));
        }
    }

    /**
     * Exige que el actor esté al menos registrado (rechaza al INVITADO).
     *
     * @param consulta descripción de lo que se consulta, para el mensaje de
     *                error ("Se requiere ser usuario registrado para
     *                &lt;consulta&gt;.").
     */
    public static void exigirRegistrado(Actor actor, String consulta) {
        if (actor.rol() == TipoUsuario.INVITADO) {
            throw new PermisoDenegadoException(
                    "Se requiere ser usuario registrado para %s.".formatted(consulta));
        }
    }
}
