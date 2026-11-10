package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Credenciales;
import com.admitienda.dominio.modelo.Deuda;
import com.admitienda.dominio.modelo.Deudor;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.PagoDeuda;
import com.admitienda.dominio.modelo.Producto;
import com.admitienda.dominio.modelo.SolicitudAdmin;
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
import com.admitienda.dominio.puerto.salida.UnidadDeTrabajo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Dobles falsos de los puertos de salida para las pruebas de la
 * aplicación: almacenan en memoria y ejecutan sin transacción real.
 */
final class Falsos {

    private Falsos() {
    }

    /** Reloj congelado en un momento fijo. */
    static final class RelojFijo implements Reloj {
        long ahora = 1_700_000_000_000L;

        @Override
        public long ahora() {
            return ahora;
        }
    }

    /** Unidad de trabajo que ejecuta el bloque directamente. */
    static final class UnidadDirecta implements UnidadDeTrabajo {
        @Override
        public <T> T ejecutar(Supplier<T> bloque) {
            return bloque.get();
        }
    }

    /** Cifrador trivial para pruebas: "hash" = contraseña + sal fija. */
    static final class CifradorFalso implements CifradorDeContrasenas {
        @Override
        public Credenciales cifrar(String contrasena) {
            return new Credenciales("salfija", contrasena + "|salfija");
        }

        @Override
        public boolean verificar(String contrasena, Credenciales almacenadas) {
            return almacenadas.hash().equals(contrasena + "|salfija");
        }
    }

    /** Almacén de usuarios en memoria. */
    static final class UsuariosEnMemoria implements RepositorioUsuarios {
        final ConcurrentHashMap<Long, Usuario> tabla = new ConcurrentHashMap<>();
        final AtomicLong secuencia = new AtomicLong();

        @Override
        public Usuario guardar(Usuario usuario) {
            long id = secuencia.incrementAndGet();
            Usuario guardado = new Usuario(id, usuario.nombreUsuario(), usuario.rol(),
                    usuario.indicio(), usuario.fechaRegistro(), usuario.credenciales());
            tabla.put(id, guardado);
            return guardado;
        }

        @Override
        public Usuario actualizar(Usuario usuario) {
            tabla.put(usuario.id(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorId(long id) {
            return Optional.ofNullable(tabla.get(id));
        }

        @Override
        public Optional<Usuario> buscarPorNombreNormalizado(String nombreNormalizado) {
            return tabla.values().stream()
                    .filter(u -> u.nombreUsuario().toLowerCase().equals(nombreNormalizado))
                    .findFirst();
        }

        @Override
        public List<Usuario> listarTodos() {
            return tabla.values().stream()
                    .sorted(Comparator.comparing(Usuario::id)).toList();
        }

        @Override
        public boolean existeNombreNormalizado(String nombreNormalizado) {
            return buscarPorNombreNormalizado(nombreNormalizado).isPresent();
        }

        @Override
        public int contarAdministradores() {
            return (int) tabla.values().stream().filter(Usuario::esAdministrador).count();
        }

        @Override
        public void eliminar(long id) {
            tabla.remove(id);
        }
    }

    /** Almacén de solicitudes en memoria. */
    static final class SolicitudesEnMemoria implements RepositorioSolicitudesAdmin {
        final ConcurrentHashMap<Long, SolicitudAdmin> tabla = new ConcurrentHashMap<>();
        final AtomicLong secuencia = new AtomicLong();

        @Override
        public SolicitudAdmin guardar(SolicitudAdmin solicitud) {
            long id = secuencia.incrementAndGet();
            SolicitudAdmin guardada = new SolicitudAdmin(id, solicitud.usuarioId(),
                    solicitud.nombreUsuario(), solicitud.estado(), solicitud.motivo(),
                    solicitud.fechaSolicitud());
            tabla.put(id, guardada);
            return guardada;
        }

        @Override
        public SolicitudAdmin actualizar(SolicitudAdmin solicitud) {
            tabla.put(solicitud.id(), solicitud);
            return solicitud;
        }

        @Override
        public Optional<SolicitudAdmin> buscarPorId(long id) {
            return Optional.ofNullable(tabla.get(id));
        }

        @Override
        public List<SolicitudAdmin> listarTodas() {
            return tabla.values().stream()
                    .sorted(Comparator.comparing(SolicitudAdmin::id)).toList();
        }

        @Override
        public boolean existePendienteDeUsuario(long usuarioId) {
            return tabla.values().stream().anyMatch(s ->
                    s.usuarioId() == usuarioId && s.estado().name().equals("PENDIENTE"));
        }

        @Override
        public void eliminarPorUsuario(long usuarioId) {
            tabla.values().removeIf(s -> s.usuarioId() == usuarioId);
        }
    }

    /** Almacén de productos en memoria. */
    static final class ProductosEnMemoria implements RepositorioProductos {
        final ConcurrentHashMap<Long, Producto> tabla = new ConcurrentHashMap<>();
        final AtomicLong secuencia = new AtomicLong();

        @Override
        public Producto guardar(Producto producto) {
            long id = secuencia.incrementAndGet();
            Producto guardado = new Producto(id, producto.tipoProducto(),
                    producto.nombreProducto(), producto.precioNeto(), producto.ganancia(),
                    producto.cantidadStock(), producto.stockMinimo());
            tabla.put(id, guardado);
            return guardado;
        }

        @Override
        public Producto actualizar(Producto producto) {
            tabla.put(producto.id(), producto);
            return producto;
        }

        @Override
        public Optional<Producto> buscarPorId(long id) {
            return Optional.ofNullable(tabla.get(id));
        }

        @Override
        public List<Producto> buscarPorNombre(String fragmento) {
            return tabla.values().stream()
                    .filter(p -> p.nombreProducto().toLowerCase()
                            .contains(fragmento.toLowerCase()))
                    .sorted(Comparator.comparing(Producto::id))
                    .toList();
        }

        @Override
        public void eliminar(long id) {
            tabla.remove(id);
        }
    }

    /** Almacén de movimientos en memoria. */
    static final class MovimientosEnMemoria implements RepositorioMovimientos {
        final List<Movimiento> tabla = new ArrayList<>();
        final AtomicLong secuencia = new AtomicLong();

        @Override
        public Movimiento guardar(Movimiento movimiento) {
            Movimiento guardado = new Movimiento(secuencia.incrementAndGet(),
                    movimiento.tipo(), movimiento.monto(), movimiento.descripcion(),
                    movimiento.fecha());
            tabla.add(guardado);
            return guardado;
        }

        @Override
        public Optional<Movimiento> buscarPorId(long id) {
            return tabla.stream().filter(m -> m.id() == id).findFirst();
        }

        @Override
        public List<Movimiento> buscarEntre(Long desde, Long hasta) {
            return tabla.stream()
                    .filter(m -> (desde == null || m.fecha() >= desde)
                            && (hasta == null || m.fecha() <= hasta))
                    .sorted(Comparator.comparing(Movimiento::fecha).reversed())
                    .toList();
        }

        @Override
        public Dinero totalDe(com.admitienda.dominio.modelo.TipoMovimiento tipo) {
            return tabla.stream().filter(m -> m.tipo() == tipo)
                    .map(Movimiento::monto)
                    .reduce(Dinero.cero(), Dinero::sumar);
        }

        @Override
        public void eliminar(long id) {
            tabla.removeIf(m -> m.id() == id);
        }
    }

    /** Almacén de deudores en memoria. */
    static final class DeudoresEnMemoria implements RepositorioDeudores {
        final ConcurrentHashMap<Long, Deudor> tabla = new ConcurrentHashMap<>();
        final AtomicLong secuencia = new AtomicLong();

        @Override
        public Deudor guardar(Deudor deudor) {
            long id = secuencia.incrementAndGet();
            Deudor guardado = new Deudor(id, deudor.nombreDeudor(),
                    deudor.nombreNormalizado());
            tabla.put(id, guardado);
            return guardado;
        }

        @Override
        public Optional<Deudor> buscarPorId(long id) {
            return Optional.ofNullable(tabla.get(id));
        }

        @Override
        public Optional<Deudor> buscarPorNombreNormalizado(String nombreNormalizado) {
            return tabla.values().stream()
                    .filter(d -> d.nombreNormalizado().equals(nombreNormalizado))
                    .findFirst();
        }

        @Override
        public List<Deudor> listarTodos() {
            return tabla.values().stream()
                    .sorted(Comparator.comparing(Deudor::id)).toList();
        }
    }

    /** Almacén de deudas en memoria. */
    static final class DeudasEnMemoria implements RepositorioDeudas {
        final ConcurrentHashMap<Long, Deuda> tabla = new ConcurrentHashMap<>();
        final AtomicLong secuencia = new AtomicLong();

        @Override
        public Deuda guardar(Deuda deuda) {
            long id = secuencia.incrementAndGet();
            Deuda guardada = new Deuda(id, deuda.deudorId(), deuda.clienteNombre(),
                    deuda.monto(), deuda.saldo(), deuda.descripcion(), deuda.fecha());
            tabla.put(id, guardada);
            return guardada;
        }

        @Override
        public Deuda actualizar(Deuda deuda) {
            tabla.put(deuda.id(), deuda);
            return deuda;
        }

        @Override
        public Optional<Deuda> buscarPorId(long id) {
            return Optional.ofNullable(tabla.get(id));
        }

        @Override
        public List<Deuda> listarPorDeudor(long deudorId) {
            return tabla.values().stream()
                    .filter(d -> d.deudorId() == deudorId)
                    .sorted(Comparator.comparing(Deuda::fecha).reversed())
                    .toList();
        }

        @Override
        public Dinero totalPendiente() {
            return tabla.values().stream().map(Deuda::saldo)
                    .reduce(Dinero.cero(), Dinero::sumar);
        }

        @Override
        public void eliminar(long id) {
            tabla.remove(id);
        }
    }

    /** Almacén de abonos en memoria. */
    static final class PagosEnMemoria implements RepositorioPagosDeuda {
        final List<PagoDeuda> tabla = new ArrayList<>();
        final AtomicLong secuencia = new AtomicLong();

        @Override
        public PagoDeuda guardar(PagoDeuda pago) {
            PagoDeuda guardado = new PagoDeuda(secuencia.incrementAndGet(),
                    pago.deudaId(), pago.monto(), pago.descripcion(), pago.fecha());
            tabla.add(guardado);
            return guardado;
        }

        @Override
        public List<PagoDeuda> listarPorDeuda(long deudaId) {
            return tabla.stream().filter(p -> p.deudaId() == deudaId).toList();
        }

        @Override
        public void eliminarPorDeuda(long deudaId) {
            tabla.removeIf(p -> p.deudaId() == deudaId);
        }
    }
}
