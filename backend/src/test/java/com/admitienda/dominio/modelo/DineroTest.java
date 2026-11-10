package com.admitienda.dominio.modelo;

import com.admitienda.dominio.modelo.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del objeto de valor Dinero: conversiones, aritmética y las
 * reglas que protegen los montos que escribe el usuario.
 */
class DineroTest {

    @Test
    void dePesosAceptaDosDecimales() {
        Dinero dinero = Dinero.dePesos(1250.5, "monto");
        assertThat(dinero.centavos()).isEqualTo(125_050);
        assertThat(dinero.pesos()).isEqualTo(1250.5);
    }

    @Test
    void dePesosAceptaValoresEnteros() {
        assertThat(Dinero.dePesos(20000, "monto").centavos()).isEqualTo(2_000_000);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, -0.01, -1000000})
    void dePesosRechazaNegativos(double valor) {
        assertThatThrownBy(() -> Dinero.dePesos(valor, "monto"))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("positivo");
    }

    @Test
    void dePesosRechazaMasDeDosDecimales() {
        assertThatThrownBy(() -> Dinero.dePesos(10.999, "monto"))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("2 decimales");
    }

    @Test
    void dePesosRechalaValoresNoFinutos() {
        assertThatThrownBy(() -> Dinero.dePesos(Double.NaN, "monto"))
                .isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> Dinero.dePesos(Double.POSITIVE_INFINITY, "monto"))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void laAritmeticaConservaLosCentavos() {
        Dinero neto = Dinero.deCentavos(100);
        Dinero ganancia = Dinero.deCentavos(50);
        assertThat(neto.sumar(ganancia).centavos()).isEqualTo(150);
        assertThat(neto.restar(ganancia).centavos()).isEqualTo(50);
    }

    @Test
    void laRestaPuedeQuedarNegativa() {
        assertThat(Dinero.deCentavos(10).restar(Dinero.deCentavos(30)).centavos())
                .isEqualTo(-20);
    }

    @Test
    void comparacionesYPredicados() {
        assertThat(Dinero.cero().esCero()).isTrue();
        assertThat(Dinero.deCentavos(1).esPositivo()).isTrue();
        assertThat(Dinero.cero().esPositivo()).isFalse();
        assertThat(Dinero.deCentavos(5).esMayorQue(Dinero.deCentavos(3))).isTrue();
        assertThat(Dinero.deCentavos(3).esMenorQue(Dinero.deCentavos(5))).isTrue();
    }

    @Test
    void dinerosIgualesSonIguales() {
        assertThat(Dinero.deCentavos(500)).isEqualTo(Dinero.deCentavos(500));
        assertThat(Dinero.deCentavos(500)).hasSameHashCodeAs(Dinero.deCentavos(500));
    }
}
