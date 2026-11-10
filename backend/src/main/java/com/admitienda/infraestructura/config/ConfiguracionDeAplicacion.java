package com.admitienda.infraestructura.config;

import com.admitienda.aplicacion.CasosDeUsoAlertas;
import com.admitienda.aplicacion.CasosDeUsoDeudas;
import com.admitienda.aplicacion.CasosDeUsoMovimientos;
import com.admitienda.aplicacion.CasosDeUsoProductos;
import com.admitienda.aplicacion.CasosDeUsoResumenes;
import com.admitienda.aplicacion.CasosDeUsoSesiones;
import com.admitienda.aplicacion.CasosDeUsoSolicitudesAdmin;
import com.admitienda.aplicacion.CasosDeUsoUsuarios;
import com.admitienda.dominio.puerto.entrada.ConsultaDeAlertas;
import com.admitienda.dominio.puerto.entrada.ConsultaDeResumenes;
import com.admitienda.dominio.puerto.entrada.GestionDeDeudas;
import com.admitienda.dominio.puerto.entrada.GestionDeMovimientos;
import com.admitienda.dominio.puerto.entrada.GestionDeProductos;
import com.admitienda.dominio.puerto.entrada.GestionDeSesiones;
import com.admitienda.dominio.puerto.entrada.GestionDeSolicitudesAdmin;
import com.admitienda.dominio.puerto.entrada.GestionDeUsuarios;
import com.admitienda.dominio.puerto.salida.CifradorDeContrasenas;
import com.admitienda.dominio.puerto.salida.RepositorioDeudas;
import com.admitienda.dominio.puerto.salida.RepositorioDeudores;
import com.admitienda.dominio.puerto.salida.RepositorioMovimientos;
import com.admitienda.dominio.puerto.salida.RepositorioPagosDeuda;
import com.admitienda.dominio.puerto.salida.RepositorioProductos;
import com.admitienda.dominio.puerto.salida.RepositorioSolicitudesAdmin;
import com.admitienda.dominio.puerto.salida.RepositorioUsuarios;
import com.admitienda.dominio.puerto.salida.Reloj;
import com.admitienda.dominio.puerto.salida.Sesiones;
import com.admitienda.dominio.puerto.salida.UnidadDeTrabajo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensamble de los casos de uso: cada puerto de entrada queda publicado
 * como un bean de Spring construido con sus puertos de salida, sin que
 * los casos de uso sepan que existen Spring ni HTTP. Los controladores
 * web dependerán de los PUERTOS de entrada, nunca de las clases
 * concretas de la aplicación.
 */
@Configuration(proxyBeanMethods = false)
public class ConfiguracionDeAplicacion {

    @Bean
    GestionDeSesiones gestionDeSesiones(RepositorioUsuarios usuarios,
                                        CifradorDeContrasenas cifrador, Sesiones sesiones) {
        return new CasosDeUsoSesiones(usuarios, cifrador, sesiones);
    }

    @Bean
    GestionDeUsuarios gestionDeUsuarios(RepositorioUsuarios usuarios,
                                        RepositorioSolicitudesAdmin solicitudes,
                                        CifradorDeContrasenas cifrador, Reloj reloj,
                                        UnidadDeTrabajo unidadDeTrabajo) {
        return new CasosDeUsoUsuarios(usuarios, solicitudes, cifrador, reloj, unidadDeTrabajo);
    }

    @Bean
    GestionDeSolicitudesAdmin gestionDeSolicitudesAdmin(
            RepositorioSolicitudesAdmin solicitudes, RepositorioUsuarios usuarios,
            Reloj reloj, UnidadDeTrabajo unidadDeTrabajo) {
        return new CasosDeUsoSolicitudesAdmin(solicitudes, usuarios, reloj, unidadDeTrabajo);
    }

    @Bean
    GestionDeProductos gestionDeProductos(RepositorioProductos productos) {
        return new CasosDeUsoProductos(productos);
    }

    @Bean
    GestionDeMovimientos gestionDeMovimientos(RepositorioMovimientos movimientos, Reloj reloj) {
        return new CasosDeUsoMovimientos(movimientos, reloj);
    }

    @Bean
    GestionDeDeudas gestionDeDeudas(RepositorioDeudores deudores, RepositorioDeudas deudas,
                                   RepositorioPagosDeuda pagos,
                                   RepositorioMovimientos movimientos, Reloj reloj,
                                   UnidadDeTrabajo unidadDeTrabajo) {
        return new CasosDeUsoDeudas(deudores, deudas, pagos, movimientos, reloj, unidadDeTrabajo);
    }

    @Bean
    ConsultaDeResumenes consultaDeResumenes(RepositorioMovimientos movimientos,
                                            RepositorioDeudas deudas, Reloj reloj) {
        return new CasosDeUsoResumenes(movimientos, deudas, reloj);
    }

    @Bean
    ConsultaDeAlertas consultaDeAlertas(RepositorioProductos productos) {
        return new CasosDeUsoAlertas(productos);
    }
}
