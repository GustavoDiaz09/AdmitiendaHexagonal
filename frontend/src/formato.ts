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

/** Convierte un valor escrito en un input a number (coma o punto). */
export function aNumero(texto: string): number {
  return Number(texto.replace(',', '.'))
}
