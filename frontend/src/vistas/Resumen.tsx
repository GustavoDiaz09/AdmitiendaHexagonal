import { useEffect, useState } from 'react'
import { api } from '../api'
import { Tabla, type Columna } from '../componentes/Tabla'
import { pesos } from '../formato'
import type { Resumen as ResumenDeTienda, ResumenPorDia, ResumenPorMes } from '../tipos'

/** Columnas compartidas por las tablas de días y de meses. */
function columnasDeTotales<T extends { ingresos: number; egresos: number; neto: number }>(
  titulo: string,
  etiqueta: (fila: T) => string,
): Columna<T>[] {
  return [
    { titulo, celda: (fila) => <span className="text-stone-700">{etiqueta(fila)}</span> },
    {
      titulo: 'Ingresos',
      celda: (fila) => <span className="text-emerald-700">{pesos(fila.ingresos)}</span>,
    },
    {
      titulo: 'Egresos',
      celda: (fila) => <span className="text-red-700">{pesos(fila.egresos)}</span>,
    },
    {
      titulo: 'Neto',
      celda: (fila) => (
        <span className={`font-medium ${fila.neto < 0 ? 'text-red-700' : 'text-stone-800'}`}>
          {pesos(fila.neto)}
        </span>
      ),
    },
  ]
}

/** Dashboard financiero: saldo de caja, por cobrar y tendencias. */
export function Resumen() {
  const [resumen, setResumen] = useState<ResumenDeTienda | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .resumen()
      .then(setResumen)
      .catch((e) => setError(e.message))
  }, [])

  if (error) {
    return <p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>
  }
  if (!resumen) {
    return <p className="text-stone-500">Cargando el resumen…</p>
  }

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold text-stone-800">Resumen de la tienda</h1>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="rounded-2xl bg-white p-6 shadow-sm">
          <p className="text-sm text-stone-500">Saldo en caja</p>
          <p className="mt-1 text-3xl font-bold text-stone-800">
            {pesos(resumen.saldoCaja)}
          </p>
        </div>
        <div className="rounded-2xl bg-white p-6 shadow-sm">
          <p className="text-sm text-stone-500">Fiado por cobrar</p>
          <p className="mt-1 text-3xl font-bold text-amber-700">
            {pesos(resumen.totalPorCobrar)}
          </p>
        </div>
      </div>

      <section>
        <h2 className="mb-4 font-semibold text-stone-700">Últimos 7 días</h2>
        <Tabla
          filas={resumen.porDia}
          columnas={columnasDeTotales<ResumenPorDia>('Día', (dia) => dia.fecha)}
          llave={(dia) => dia.fecha}
          mensajeVacio="Sin movimientos esta semana."
        />
      </section>

      <section>
        <h2 className="mb-4 font-semibold text-stone-700">Últimos 12 meses</h2>
        <Tabla
          filas={resumen.porMes}
          columnas={columnasDeTotales<ResumenPorMes>('Mes', (mes) => mes.mes)}
          llave={(mes) => mes.mes}
          mensajeVacio="Sin movimientos este año."
        />
      </section>
    </div>
  )
}
