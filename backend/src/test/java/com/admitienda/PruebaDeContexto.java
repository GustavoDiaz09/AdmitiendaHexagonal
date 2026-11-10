package com.admitienda;

import com.admitienda.infraestructura.seguridad.AdaptadorCifradorPbkdf2;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de contexto: levanta la aplicación completa (adaptadores
 * incluidos) sobre una base SQLite de prueba y recorre el contrato REST
 * de punta a punta con MockMvc. Cada ejecución usa una base nueva para
 * que la semilla de datos quede determinista.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = PruebaDeContexto.BaseNueva.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PruebaDeContexto {

    /** Hace que la base de la ejecución sea un archivo único en target. */
    static class BaseNueva implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext contexto) {
            TestPropertyValues.of(
                    "spring.datasource.url=jdbc:sqlite:target/prueba-"
                            + System.currentTimeMillis() + ".db",
                    "admitienda.admin-inicial.nombre-usuario=admin",
                    "admitienda.admin-inicial.contrasena=admin123")
                    .applyTo(contexto.getEnvironment());
        }
    }

    /** PBKDF2 real para verificar el flujo criptográfico completo. */
    @TestConfiguration
    static class PruebaDeCifradoReal {
        @Bean
        com.admitienda.dominio.puerto.salida.CifradorDeContrasenas cifradorDePrueba() {
            return new AdaptadorCifradorPbkdf2();
        }
    }

    @Autowired
    private MockMvc mvc;

    private String tokenAdmin() throws Exception {
        String cuerpo = mvc.perform(post("/api/sesiones").contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"admin\",\"contrasena\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return extraer(cuerpo, "token");
    }

    private String extraer(String json, String campo) {
        String marcador = "\"" + campo + "\":\"";
        int inicio = json.indexOf(marcador) + marcador.length();
        return json.substring(inicio, json.indexOf('"', inicio));
    }

    @Test
    @Order(1)
    void laSemillaCreaElAdminYLosDatosDemo() throws Exception {
        String token = tokenAdmin();
        mvc.perform(get("/api/productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
        mvc.perform(get("/api/alertas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/deudores").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Order(2)
    void elCatalogoEsPublicoSinSesion() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreProducto").exists());
        mvc.perform(get("/api/productos?nombre=coca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreProducto").value("Coca-Cola 400 ml"));
    }

    @Test
    @Order(3)
    void sinSesionSeResponde401() throws Exception {
        mvc.perform(get("/api/movimientos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(4)
    void registroLoginYSolicitudDePermiso() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"luis\",\"contrasena\":\"luis12345\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("REGISTRADO"));

        String tokenLuis = extraer(mvc.perform(post("/api/sesiones")
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"luis\",\"contrasena\":\"luis12345\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "token");

        mvc.perform(get("/api/movimientos").header("Authorization", "Bearer " + tokenLuis))
                .andExpect(status().isOk());

        mvc.perform(post("/api/solicitudes-admin")
                        .header("Authorization", "Bearer " + tokenLuis)
                        .contentType(APPLICATION_JSON)
                        .content("{\"motivo\":\"ayudo en la tienda\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        mvc.perform(post("/api/solicitudes-admin")
                        .header("Authorization", "Bearer " + tokenLuis)
                        .contentType(APPLICATION_JSON)
                        .content("{\"motivo\":\"otra vez\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(5)
    void elRegistroRechazaNombresOcupados() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"luis\",\"contrasena\":\"luis12345\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(6)
    void elAdminApruebaLaSolicitudYPromueve() throws Exception {
        String token = tokenAdmin();
        String cuerpo = mvc.perform(get("/api/solicitudes-admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long id = Long.parseLong(extraerNumero(cuerpo));

        mvc.perform(post("/api/solicitudes-admin/" + id + "/aprobar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    private String extraerNumero(String json) {
        int inicio = json.indexOf("\"id\":") + 5;
        return json.substring(inicio, json.indexOf(',', inicio)).trim();
    }

    @Test
    @Order(7)
    void crudDeProductosYReglasDeMontos() throws Exception {
        String token = tokenAdmin();

        mvc.perform(post("/api/productos").header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"tipoProducto\":\"Lacteos\",\"nombreProducto\":\"Yogurt\"," +
                                "\"precioNeto\":1000.5,\"ganancia\":200,\"cantidadStock\":5," +
                                "\"stockMinimo\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precioVenta").value(1200.5));

        mvc.perform(post("/api/productos").header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"tipoProducto\":\"X\",\"nombreProducto\":\"Mal\"," +
                                "\"precioNeto\":10.999,\"ganancia\":1,\"cantidadStock\":1," +
                                "\"stockMinimo\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    void flujoDeCajaYFiadoConAbono() throws Exception {
        String token = tokenAdmin();

        String respuesta = mvc.perform(post("/api/deudas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"nombreDeudor\":\"  Luisa  Gomez \",\"monto\":8000," +
                                "\"descripcion\":\"mercado\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(8000))
                .andReturn().getResponse().getContentAsString();
        long idDeuda = Long.parseLong(extraerNumero(respuesta));

        mvc.perform(post("/api/deudas/" + idDeuda + "/abonos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"monto\":3000,\"descripcion\":\"abono parcial\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(5000))
                .andExpect(jsonPath("$.pagos[0].monto").value(3000));

        mvc.perform(get("/api/movimientos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movimientos[0].tipo").value("INGRESO"))
                .andExpect(jsonPath("$.movimientos[0].descripcion")
                        .value("Pago de deuda de Luisa Gomez: abono parcial"));

        mvc.perform(delete("/api/deudas/" + idDeuda)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    void elUltimoAdministradorEstaProtegido() throws Exception {
        String token = tokenAdmin();

        mvc.perform(put("/api/usuarios/1/rol").header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"rol\":\"REGISTRADO\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(delete("/api/usuarios/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    void cerrarSesionInvalidaElToken() throws Exception {
        String token = tokenAdmin();

        mvc.perform(delete("/api/sesiones").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/sesiones/actual").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(11)
    void elResumenDevuelveLosUltimosSieteDias() throws Exception {
        String token = tokenAdmin();

        mvc.perform(get("/api/resumenes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoCaja").exists())
                .andExpect(jsonPath("$.porDia.length()").value(7))
                .andExpect(jsonPath("$.porMes.length()").value(12));
    }
}
