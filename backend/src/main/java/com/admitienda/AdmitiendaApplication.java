package com.admitienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de Admitienda.
 *
 * <p>El núcleo del negocio vive en {@code com.admitienda.dominio} y
 * {@code com.admitienda.aplicacion} sin conocer Spring; esta clase y el
 * paquete {@code com.admitienda.infraestructura} son los adaptadores que
 * conectan el hexágono con el mundo exterior (HTTP, SQLite y reloj).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AdmitiendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmitiendaApplication.class, args);
    }
}
