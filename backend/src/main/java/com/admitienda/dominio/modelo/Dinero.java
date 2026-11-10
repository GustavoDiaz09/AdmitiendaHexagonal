package com.admitienda.dominio.modelo;

import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Objeto de valor para dinero en pesos colombianos.
 *
 * <p>Guarda el importe en centavos (entero largo) para eliminar los errores
 * de redondeo del punto flotante. Las entradas del usuario se crean con
 * {@link #dePesos(double, String)}, que valida que el número sea finito, no
 * negativo y tenga a lo sumo dos decimales; las operaciones internas
 * (sumas, restas y lecturas de base de datos) pueden producir valores
 * negativos, por ejemplo cuando los egresos de la caja superan los
 * ingresos.</p>
 */
public final class Dinero implements Comparable<Dinero> {

    private final long centavos;

    private Dinero(long centavos) {
        this.centavos = centavos;
    }

    /** Crea un dinero a partir de centavos ya validados (persistencia o cálculos). */
    public static Dinero deCentavos(long centavos) {
        return new Dinero(centavos);
    }

    /** Dinero de valor cero. */
    public static Dinero cero() {
        return new Dinero(0);
    }

    /**
     * Crea un dinero a partir de un monto en pesos escrito por el usuario.
     *
     * @param pesos      valor en pesos; se rechazan NaN, infinitos, negativos
     *                   y más de dos decimales.
     * @param nombreCampo nombre del campo para los mensajes de error.
     * @throws ReglaDeNegocioException si el monto no es un dinero válido.
     */
    public static Dinero dePesos(double pesos, String nombreCampo) {
        if (Double.isNaN(pesos) || Double.isInfinite(pesos)) {
            throw new ReglaDeNegocioException(
                    "El campo %s debe contener un número válido.".formatted(nombreCampo));
        }
        BigDecimal exacto = BigDecimal.valueOf(pesos);
        if (exacto.scale() > 2) {
            throw new ReglaDeNegocioException(
                    "El campo %s solo puede tener hasta 2 decimales.".formatted(nombreCampo));
        }
        if (exacto.signum() < 0) {
            throw new ReglaDeNegocioException(
                    "El campo %s debe ser un valor positivo.".formatted(nombreCampo));
        }
        try {
            return new Dinero(exacto.movePointRight(2).longValueExact());
        } catch (ArithmeticException e) {
            throw new ReglaDeNegocioException(
                    "El campo %s es demasiado grande.".formatted(nombreCampo));
        }
    }

    /** Suma otro dinero a este. */
    public Dinero sumar(Dinero otro) {
        return new Dinero(centavos + Objects.requireNonNull(otro, "otro").centavos);
    }

    /** Resta otro dinero a este (puede quedar negativo, p. ej. saldo de caja). */
    public Dinero restar(Dinero otro) {
        return new Dinero(centavos - Objects.requireNonNull(otro, "otro").centavos);
    }

    /** Indica si el importe es mayor que cero. */
    public boolean esPositivo() {
        return centavos > 0;
    }

    /** Indica si el importe es exactamente cero. */
    public boolean esCero() {
        return centavos == 0;
    }

    /** Indica si este importe es mayor que otro. */
    public boolean esMayorQue(Dinero otro) {
        return compareTo(otro) > 0;
    }

    /** Indica si este importe es menor que otro. */
    public boolean esMenorQue(Dinero otro) {
        return compareTo(otro) < 0;
    }

    /** Importe en centavos, para persistencia. */
    public long centavos() {
        return centavos;
    }

    /** Importe en pesos, para exponer en la API. */
    public double pesos() {
        return centavos / 100.0;
    }

    @Override
    public int compareTo(Dinero otro) {
        return Long.compare(centavos, otro.centavos);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Dinero otro && centavos == otro.centavos;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(centavos);
    }

    @Override
    public String toString() {
        return BigDecimal.valueOf(centavos, 2).toPlainString();
    }
}
