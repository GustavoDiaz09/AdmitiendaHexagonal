package com.admitienda.infraestructura.config;

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
import com.admitienda.infraestructura.persistencia.AdaptadorDeudasJdbc;
import com.admitienda.infraestructura.persistencia.AdaptadorDeudoresJdbc;
import com.admitienda.infraestructura.persistencia.AdaptadorMovimientosJdbc;
import com.admitienda.infraestructura.persistencia.AdaptadorPagosDeudaJdbc;
import com.admitienda.infraestructura.persistencia.AdaptadorProductosJdbc;
import com.admitienda.infraestructura.persistencia.AdaptadorSolicitudesJdbc;
import com.admitienda.infraestructura.persistencia.AdaptadorUnidadDeTrabajo;
import com.admitienda.infraestructura.persistencia.AdaptadorUsuariosJdbc;
import com.admitienda.infraestructura.seguridad.AdaptadorCifradorPbkdf2;
import com.admitienda.infraestructura.seguridad.AdaptadorSesionesEnMemoria;
import com.admitienda.infraestructura.tiempo.RelojDelSistema;
import com.admitienda.infraestructura.web.FiltroDeSesion;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Ensamble de los adaptadores de salida: aquÃ­ (y solo aquÃ­) el cÃ³digo
 * de infraestructura se conecta con tecnologÃ­as concretas â€” SQLite vÃ­a
 * JdbcTemplate, PBKDF2, sesiones en memoria, transacciones de Spring.
 * El resto del sistema solo ve los puertos.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PropiedadesAdmitienda.class)
public class ConfiguracionDeInfraestructura {

    @Bean
    Reloj relojDelSistema() {
        return new RelojDelSistema();
    }

    @Bean
    CifradorDeContrasenas cifradorDeContrasenas() {
        return new AdaptadorCifradorPbkdf2();
    }

    @Bean
    Sesiones sesiones(Reloj reloj, PropiedadesAdmitienda propiedades) {
        return new AdaptadorSesionesEnMemoria(reloj,
                propiedades.duracionSesionHoras() * 3_600_000L);
    }

    @Bean
    RepositorioUsuarios repositorioUsuarios(JdbcTemplate jdbc) {
        return new AdaptadorUsuariosJdbc(jdbc);
    }

    @Bean
    RepositorioSolicitudesAdmin repositorioSolicitudesAdmin(
            JdbcTemplate jdbc) {
        return new AdaptadorSolicitudesJdbc(jdbc);
    }

    @Bean
    RepositorioProductos repositorioProductos(JdbcTemplate jdbc) {
        return new AdaptadorProductosJdbc(jdbc);
    }

    @Bean
    RepositorioMovimientos repositorioMovimientos(
            JdbcTemplate jdbc) {
        return new AdaptadorMovimientosJdbc(jdbc);
    }

    @Bean
    RepositorioDeudores repositorioDeudores(JdbcTemplate jdbc) {
        return new AdaptadorDeudoresJdbc(jdbc);
    }

    @Bean
    RepositorioDeudas repositorioDeudas(JdbcTemplate jdbc) {
        return new AdaptadorDeudasJdbc(jdbc);
    }

    @Bean
    RepositorioPagosDeuda repositorioPagosDeuda(JdbcTemplate jdbc) {
        return new AdaptadorPagosDeudaJdbc(jdbc);
    }

    @Bean
    UnidadDeTrabajo unidadDeTrabajo(PlatformTransactionManager administradorDeTransacciones) {
        return new AdaptadorUnidadDeTrabajo(new TransactionTemplate(administradorDeTransacciones));
    }

    @Bean
    FilterRegistrationBean<FiltroDeSesion> filtroDeSesion(Sesiones sesiones) {
        FilterRegistrationBean<FiltroDeSesion> registro =
                new FilterRegistrationBean<>(new FiltroDeSesion(sesiones));
        registro.addUrlPatterns("/api/*");
        registro.setOrder(1);
        return registro;
    }

    @Bean
    WebMvcConfigurer corsDeAdmitienda(PropiedadesAdmitienda propiedades) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registro) {
                registro.addMapping("/api/**")
                        .allowedOrigins(propiedades.corsOrigenes().toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }

    @Bean
    CommandLineRunner iniciadorDeDatos(RepositorioUsuarios usuarios,
                                        RepositorioSolicitudesAdmin solicitudes,
                                        RepositorioProductos productos,
                                        RepositorioMovimientos movimientos,
                                        RepositorioDeudores deudores,
                                        RepositorioDeudas deudas,
                                        RepositorioPagosDeuda pagos,
                                        CifradorDeContrasenas cifrador,
                                        Reloj reloj,
                                        PropiedadesAdmitienda propiedades) {
        return new IniciadorDeDatos(usuarios, solicitudes, productos, movimientos,
                deudores, deudas, pagos, cifrador, reloj, propiedades);
    }
}
