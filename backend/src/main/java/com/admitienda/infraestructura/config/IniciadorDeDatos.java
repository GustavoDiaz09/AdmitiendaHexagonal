package com.admitienda.infraestructura.config;

import com.admitienda.dominio.modelo.Deuda;
import com.admitienda.dominio.modelo.Deudor;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.EstadoSolicitud;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.PagoDeuda;
import com.admitienda.dominio.modelo.Producto;
import com.admitienda.dominio.modelo.SolicitudAdmin;
import com.admitienda.dominio.modelo.TipoMovimiento;
import com.admitienda.dominio.modelo.TipoUsuario;
import com.admitienda.dominio.modelo.Usuario;
import com.admitienda.dominio.puerto.salida.CifradorDeContrasenas;
import com.admitienda.dominio.puerto.salida.RepositorioDeudas;
import com.admitienda.dominio.puerto.salida.RepositorioDeudores;
import com.admitienda.dominio.puerto.salida.RepositorioMovimientos;
import com.admitienda.dominio.puerto.salida.RepositorioPagosDeuda;
import com.admitienda.dominio.puerto.salida.RepositorioProductos;
import com.admitienda.dominio.puerto.salida.RepositorioSolicitudesAdmin;
import com.admitienda.dominio.puerto.salida.RepositorioUsuarios;
import com.admitienda.dominio.puerto.salida.Reloj;
import org.springframework.boot.CommandLineRunner;

/**
 * Adaptador de arranque: siembra la base vacía con el administrador
 * inicial (tomado de la configuración) y datos de demostración para
 * que la tienda se pueda explorar desde el primer arranque. Si ya hay
 * usuarios, no toca nada.
 */
public final class IniciadorDeDatos implements CommandLineRunner {

    private static final long DIA = 24L * 60 * 60 * 1000;

    private final RepositorioUsuarios usuarios;
    private final RepositorioSolicitudesAdmin solicitudes;
    private final RepositorioProductos productos;
    private final RepositorioMovimientos movimientos;
    private final RepositorioDeudores deudores;
    private final RepositorioDeudas deudas;
    private final RepositorioPagosDeuda pagos;
    private final CifradorDeContrasenas cifrador;
    private final Reloj reloj;
    private final PropiedadesAdmitienda propiedades;

    public IniciadorDeDatos(RepositorioUsuarios usuarios,
                            RepositorioSolicitudesAdmin solicitudes,
                            RepositorioProductos productos,
                            RepositorioMovimientos movimientos,
                            RepositorioDeudores deudores,
                            RepositorioDeudas deudas,
                            RepositorioPagosDeuda pagos,
                            CifradorDeContrasenas cifrador,
                            Reloj reloj,
                            PropiedadesAdmitienda propiedades) {
        this.usuarios = usuarios;
        this.solicitudes = solicitudes;
        this.productos = productos;
        this.movimientos = movimientos;
        this.deudores = deudores;
        this.deudas = deudas;
        this.pagos = pagos;
        this.cifrador = cifrador;
        this.reloj = reloj;
        this.propiedades = propiedades;
    }

    @Override
    public void run(String... argumentos) {
        if (!usuarios.listarTodos().isEmpty()) {
            return;
        }
        long ahora = reloj.ahora();
        sembrarUsuarios(ahora);
        sembrarProductos();
        sembrarCaja(ahora);
        sembrarCartera(ahora);
    }

    private void sembrarUsuarios(long ahora) {
        var admin = propiedades.adminInicial();
        crearUsuario(admin.nombreUsuario(), admin.contrasena(), TipoUsuario.ADMIN,
                ahora - 30 * DIA);
        Usuario ana = crearUsuario("ana", "ana123", TipoUsuario.REGISTRADO, ahora - 5 * DIA);
        solicitudes.guardar(new SolicitudAdmin(null, ana.id(), ana.nombreUsuario(),
                EstadoSolicitud.PENDIENTE,
                "Soy la hija del dueño y ayudo en la tienda los fines de semana.",
                ahora - 4 * DIA));
    }

    private Usuario crearUsuario(String nombre, String contrasena, TipoUsuario rol, long fecha) {
        return usuarios.guardar(new Usuario(null, nombre, rol, "", fecha,
                cifrador.cifrar(contrasena)));
    }

    private void sembrarProductos() {
        producto("Gaseosas", "Coca-Cola 400 ml", 2500, 800, 24, 6);
        producto("Gaseosas", "Sprite 400 ml", 2500, 800, 18, 6);
        producto("Bebidas", "Agua Brisa 600 ml", 1500, 500, 30, 10);
        producto("Snacks", "Papas Margarita 45 g", 1800, 700, 15, 5);
        producto("Snacks", "Cheetos 34 g", 1900, 750, 4, 5);
        producto("Dulces", "Chocolatina Jet 22 g", 1000, 400, 40, 10);
        producto("Dulces", "Bombón Bon Bon Bum", 800, 300, 3, 6);
        producto("Panadería", "Pan Bimbo grande", 4800, 900, 8, 3);
        producto("Lácteos", "Leche Alquería 1 L", 4200, 600, 12, 4);
        producto("Abarrotes", "Café Águila Roja 100 g", 6500, 1200, 10, 3);
        producto("Abarrotes", "Arroz Diana 500 g", 2400, 350, 25, 8);
        producto("Abarrotes", "Atún Van Camps 140 g", 4900, 1100, 2, 4);
    }

    private void producto(String tipo, String nombre, int neto, int ganancia,
                           int stock, int minimo) {
        productos.guardar(new Producto(null, tipo, nombre, pesos(neto),
                pesos(ganancia), stock, minimo));
    }

    private void sembrarCaja(long ahora) {
        ingreso(150_000, "Ventas del día", ahora - 12 * 60 * 60 * 1000);
        egreso(80_000, "Compra de mercancía en la central", ahora - 2 * DIA);
        ingreso(120_000, "Ventas del día", ahora - 5 * DIA);
        egreso(90_000, "Pago de energía eléctrica", ahora - 20 * DIA);
        ingreso(180_000, "Ventas del día", ahora - 35 * DIA);
        egreso(700_000, "Arriendo del local", ahora - 40 * DIA);
        ingreso(200_000, "Ventas del día", ahora - 65 * DIA);
    }

    private void sembrarCartera(long ahora) {
        var maria = deudores.guardar(new Deudor(null, "María Pérez", "maría pérez"));
        var deudaMaria = deudas.guardar(new Deuda(null, maria.id(), maria.nombreDeudor(),
                pesos(50_000), pesos(30_000),
                "Mercado fiado: arroz, aceite y gaseosas", ahora - 6 * DIA));
        pagos.guardar(new PagoDeuda(null, deudaMaria.id(), pesos(20_000),
                "Abono en efectivo", ahora - 2 * DIA));
        ingreso(20_000, "Pago de deuda de María Pérez: Abono en efectivo", ahora - 2 * DIA);

        var carlos = deudores.guardar(new Deudor(null, "Carlos Rojas", "carlos rojas"));
        var deudaCarlos = deudas.guardar(new Deuda(null, carlos.id(), carlos.nombreDeudor(),
                pesos(25_000), pesos(0), "Fiado de pantuflas y medias",
                ahora - 15 * DIA));
        pagos.guardar(new PagoDeuda(null, deudaCarlos.id(), pesos(25_000),
                "Cancelación total", ahora - 10 * DIA));
        ingreso(25_000, "Pago de deuda de Carlos Rojas: Cancelación total", ahora - 10 * DIA);
    }

    private void ingreso(int pesos, String descripcion, long fecha) {
        movimientos.guardar(new Movimiento(null, TipoMovimiento.INGRESO,
                pesos(pesos), descripcion, fecha));
    }

    private void egreso(int pesos, String descripcion, long fecha) {
        movimientos.guardar(new Movimiento(null, TipoMovimiento.EGRESO,
                pesos(pesos), descripcion, fecha));
    }

    /** Dinero de un valor entero en pesos (la semilla no usa centavos). */
    private static Dinero pesos(int valor) {
        return Dinero.deCentavos(valor * 100L);
    }
}
