/** Formateadores compartidos: pesos colombianos y fechas hora Colombia. */

const formatoPesos = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
})

const formatoPesosConCentavos = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 2,
})

const formatoDia = new Intl.DateTimeFormat('es-CO', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'America/Bogota',
})

/** Monto en pesos, sin decimales (lo habitual en tienda). */
export function pesos(monto: number): string {
  return formatoPesos.format(monto)
}

/** Monto en pesos con centavos, para montos que los traen. */
export function pesosExactos(monto: number): string {
  const texto = formatoPesosConCentavos.format(monto)
  return texto.endsWith(',00') ? formatoPesos.format(monto) : texto
}

/** Fecha legible en hora Colombia. */
export function fecha(milisegundos: number): string {
  return formatoDia.format(new Date(milisegundos))
}

const PATRON_MILES = /^\d{1,3}(\.\d{3})+$/
const PATRON_DECIMAL = /^\d+(\.\d+)?$/

/**
 * Convierte lo escrito en un input de dinero a number, con formato colombiano:
 * el punto agrupa miles ("15.000" = 15000) y la coma es el decimal ("12.500,5").
 * Devuelve NaN si el texto no es un número válido, en vez de adivinar.
 */
export function aNumero(texto: string): number {
  const limpio = texto.trim()
  if (limpio.includes(',')) {
    const normalizado = limpio.replace(/\./g, '').replace(',', '.')
    return PATRON_DECIMAL.test(normalizado) ? Number(normalizado) : Number.NaN
  }
  if (PATRON_MILES.test(limpio)) {
    return Number(limpio.replace(/\./g, ''))
  }
  return PATRON_DECIMAL.test(limpio) ? Number(limpio) : Number.NaN
}
