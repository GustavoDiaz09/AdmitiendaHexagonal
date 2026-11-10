/** Tipos del contrato REST de Admitienda (docs/CONTRATO_API.md). */

export type Rol = 'INVITADO' | 'REGISTRADO' | 'ADMIN'
export type TipoMovimiento = 'INGRESO' | 'EGRESO'
export type EstadoSolicitud = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA'

export interface Usuario {
  id: number
  nombreUsuario: string
  rol: Rol
  fechaRegistro: number
}

export interface Sesion {
  token: string
  usuario: Usuario
}

export interface Producto {
  id: number
  tipoProducto: string
  nombreProducto: string
  precioNeto: number
  ganancia: number
  precioVenta: number
  cantidadStock: number
  stockMinimo: number
  enAlerta: boolean
}

export interface Movimiento {
  id: number
  tipo: TipoMovimiento
  monto: number
  descripcion: string
  fecha: number
}

export interface ListadoDeMovimientos {
  saldoCaja: number
  movimientos: Movimiento[]
}

export interface SolicitudAdmin {
  id: number
  usuarioId: number
  nombreUsuario: string
  estado: EstadoSolicitud
  fechaSolicitud: number
  motivo: string
}

export interface DeudorResumen {
  id: number
  nombreDeudor: string
  deudasPendientes: number
  totalPendiente: number
  fechaUltimaDeuda: number
}

export interface Pago {
  id: number
  monto: number
  fecha: number
}

export interface DeudaConPagos {
  id: number
  monto: number
  saldo: number
  descripcion: string
  fecha: number
  pagos: Pago[]
}

export interface HistorialDeDeudor {
  id: number
  nombreDeudor: string
  deudas: DeudaConPagos[]
}

export interface ResumenPorDia {
  fecha: string
  ingresos: number
  egresos: number
  neto: number
}

export interface ResumenPorMes {
  mes: string
  ingresos: number
  egresos: number
  neto: number
}

export interface Resumen {
  saldoCaja: number
  totalPorCobrar: number
  porDia: ResumenPorDia[]
  porMes: ResumenPorMes[]
}

export interface Alerta {
  id: number
  nombreProducto: string
  tipoProducto: string
  cantidadStock: number
  stockMinimo: number
  faltante: number
}
