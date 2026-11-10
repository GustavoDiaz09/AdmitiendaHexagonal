/**
 * Cliente HTTP del contrato REST: un único punto por donde viajan las
 * peticiones, con el token de sesión inyectado y errores como texto
 * legible (el contrato responde {"error": mensaje}).
 */

import type {
  Alerta,
  DeudaConPagos,
  DeudorResumen,
  HistorialDeDeudor,
  ListadoDeMovimientos,
  Movimiento,
  Producto,
  Resumen,
  Rol,
  Sesion,
  SolicitudAdmin,
  TipoMovimiento,
  Usuario,
} from './tipos'

const RUTA_BASE = '/api'
const CLAVE_TOKEN = 'admitienda.token'

export class ErrorDeApi extends Error {
  readonly estado: number

  constructor(mensaje: string, estado: number) {
    super(mensaje)
    this.estado = estado
  }
}

function guardarToken(token: string) {
  localStorage.setItem(CLAVE_TOKEN, token)
}

export function tokenActual(): string | null {
  return localStorage.getItem(CLAVE_TOKEN)
}

export function olvidarToken() {
  localStorage.removeItem(CLAVE_TOKEN)
}

async function pedir<T>(ruta: string, init?: RequestInit): Promise<T> {
  const cabeceras = new Headers(init?.headers)
  cabeceras.set('Content-Type', 'application/json')
  const token = tokenActual()
  if (token) {
    cabeceras.set('Authorization', `Bearer ${token}`)
  }
  const respuesta = await fetch(RUTA_BASE + ruta, { ...init, headers: cabeceras })
  if (respuesta.status === 204) {
    return undefined as T
  }
  const cuerpo = await respuesta.json().catch(() => null)
  if (!respuesta.ok) {
    throw new ErrorDeApi(cuerpo?.error ?? 'Error inesperado del servidor.', respuesta.status)
  }
  return cuerpo as T
}

const obtener = <T>(ruta: string) => pedir<T>(ruta)
const publicar = <T>(ruta: string, cuerpo: unknown) =>
  pedir<T>(ruta, { method: 'POST', body: JSON.stringify(cuerpo) })
const actualizar = <T>(ruta: string, cuerpo: unknown) =>
  pedir<T>(ruta, { method: 'PUT', body: JSON.stringify(cuerpo) })
const borrar = (ruta: string) => pedir<void>(ruta, { method: 'DELETE' })

type UsuarioSinFecha = Omit<Usuario, 'fechaRegistro'>
type DatosDeProducto = Omit<Producto, 'id' | 'precioVenta' | 'enAlerta'>

/** Endpoint del contrato REST, agrupados por recurso. */
export const api = {
  guardarToken,

  iniciarSesion: (nombreUsuario: string, contrasena: string) =>
    publicar<Sesion>('/sesiones', { nombreUsuario, contrasena }),

  cerrarSesion: () => borrar('/sesiones'),

  sesionActual: () => obtener<{ usuario: UsuarioSinFecha }>('/sesiones/actual'),

  registrarUsuario: (nombreUsuario: string, contrasena: string, indicio: string) =>
    publicar<Usuario>('/usuarios', { nombreUsuario, contrasena, indicio }),

  listarUsuarios: () => obtener<Usuario[]>('/usuarios'),

  cambiarRol: (id: number, rol: Rol) =>
    actualizar<Usuario>(`/usuarios/${id}/rol`, { rol }),

  eliminarUsuario: (id: number) => borrar(`/usuarios/${id}`),

  solicitarPermiso: (motivo: string) =>
    publicar<SolicitudAdmin>('/solicitudes-admin', { motivo }),

  listarSolicitudes: () => obtener<SolicitudAdmin[]>('/solicitudes-admin'),

  aprobarSolicitud: (id: number) =>
    publicar<SolicitudAdmin>(`/solicitudes-admin/${id}/aprobar`, {}),

  rechazarSolicitud: (id: number) =>
    publicar<SolicitudAdmin>(`/solicitudes-admin/${id}/rechazar`, {}),

  listarProductos: (nombre?: string) =>
    obtener<Producto[]>(`/productos${nombre ? `?nombre=${encodeURIComponent(nombre)}` : ''}`),

  crearProducto: (datos: DatosDeProducto) => publicar<Producto>('/productos', datos),

  editarProducto: (id: number, datos: DatosDeProducto) =>
    actualizar<Producto>(`/productos/${id}`, datos),

  eliminarProducto: (id: number) => borrar(`/productos/${id}`),

  listarMovimientos: (desde?: number, hasta?: number) => {
    const parametros = new URLSearchParams()
    if (desde !== undefined) parametros.set('desde', String(desde))
    if (hasta !== undefined) parametros.set('hasta', String(hasta))
    const consulta = parametros.size ? `?${parametros}` : ''
    return obtener<ListadoDeMovimientos>(`/movimientos${consulta}`)
  },

  crearMovimiento: (tipo: TipoMovimiento, monto: number, descripcion: string) =>
    publicar<Movimiento>('/movimientos', { tipo, monto, descripcion }),

  eliminarMovimiento: (id: number) => borrar(`/movimientos/${id}`),

  listarDeudores: () => obtener<DeudorResumen[]>('/deudores'),

  historialDeDeudor: (id: number) => obtener<HistorialDeDeudor>(`/deudores/${id}`),

  registrarDeuda: (nombreDeudor: string, monto: number, descripcion: string) =>
    publicar<DeudaConPagos>('/deudas', { nombreDeudor, monto, descripcion }),

  editarDeuda: (id: number, monto: number, descripcion: string) =>
    actualizar<DeudaConPagos>(`/deudas/${id}`, { monto, descripcion }),

  abonarDeuda: (id: number, monto: number, descripcion: string) =>
    publicar<DeudaConPagos>(`/deudas/${id}/abonos`, { monto, descripcion }),

  eliminarDeuda: (id: number) => borrar(`/deudas/${id}`),

  resumen: () => obtener<Resumen>('/resumenes'),

  alertas: () => obtener<Alerta[]>('/alertas'),
}
