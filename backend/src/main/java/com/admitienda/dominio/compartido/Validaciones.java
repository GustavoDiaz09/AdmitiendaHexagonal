package com.admitienda.dominio.compartido;

import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;

/**
 * Validaciones de datos de entrada compartidas por los casos de uso.
 *
 * <p>Cada método lanza {@link ReglaDeNegocioException} con un mensaje
 * legible en español, en lugar de devolver códigos de error.</p>
 */
public final class Validaciones {

    private Validaciones() {
    }

    /** Exige un texto no vacío (sin contar espacios de los extremos). */
    public static String requerirTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDeNegocioException(
                    "El campo %s no puede quedar vacío.".formatted(nombreCampo));
        }
        return valor.trim();
    }

    /** Exige una cantidad entera no negativa. */
    public static int exigirNoNegativo(int valor, String nombreCampo) {
        if (valor < 0) {
            throw new ReglaDeNegocioException(
                    "El campo %s debe ser un valor positivo.".formatted(nombreCampo));
        }
        return valor;
    }

    /**
     * Normaliza un nombre para comparar identidades sin distinguir
     * mayúsculas ni espacios repetidos ("María  Pérez" y "maría pérez"
     * producen el mismo nombre normalizado).
     */
    public static String normalizarNombre(String valor) {
        return (valor == null ? "" : valor).trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
