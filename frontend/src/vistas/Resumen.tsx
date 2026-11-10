import { useEffect, useState } from 'react'
import { api } from '../api'
import { Aviso, Cabecera } from '../componentes/ui'
import { Tabla, type Columna } from '../componentes/Tabla'
import { etiquetaDeMes, etiquetaDia, pesos } from '../formato'
import type { Resumen as ResumenDeTienda, ResumenPorDia, ResumenPorMes } from '../tipos'

/** Columnas compartidas por las tablas de días y de meses. */
function columnasDeTotales<T extends { ingresos: number; egresos: number; neto: number }>(
  titulo: string,
  etiqueta: (fila: T) => string,
): Columna<T>[] {
  return [
    { titulo, celda: (fila) => <span className="text-stone-700">{etiqueta(fila)}</span> },
    {
      titulo: 'Entradas',
      derecha: true,
      celda: (fila) => <span className="tabular-nums text-emerald-800">{pesos(fila.ingresos)}</span>,
    },
    {
      titulo: 'Salidas',
      derecha: true,
      celda: (fila) => <span className="tabular-nums text-red-700">{pesos(fila.egresos)}</span>,
    },
    {
      titulo: 'Neto',
      derecha: true,
      celda: (fila) => (
        <span className={`font-semibold tabular-nums ${fila.neto < 0 ? 'text-red-700' : 'text-stone-900'}`}>
          {pesos(fila.neto)}
        </span>
      ),
    },
  ]
}

/** Franja de cifras: una sola línea dividida por filetes, sin tarjetas grandes. */
function Cifra({ etiqueta, valor, tono = 'text-stone-900' }: { etiqueta: string; valor: string; tono?: string }) {
  return (
    <div className="flex min-w-0 flex-1 flex-col gap-0.5 px-5 first:pl-0 last:pr-0">
      <span className="text-[11px] font-semibold uppercase tracking-wider text-stone-500">{etiqueta}</span>
      <span className={`truncate text-xl font-semibold tabular-nums ${tono}`}>{valor}</span>
    </div>
  )
}

/** Resumen financiero compacto: cifras clave arriba y tendencias en tablas densas. */
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
    return <Aviso tono="error">{error}</Aviso>
  }

  const netoSemana = resumen?.porDia.reduce((total, dia) => total + dia.neto, 0) ?? 0

  return (
    <>
      <Cabecera titulo="Resumen" />

      <div className="flex flex-wrap divide-stone-200 rounded-xl border border-stone-200/80 bg-white px-5 py-4 sm:flex-nowrap sm:divide-x">
        <Cifra
          etiqueta="Saldo en caja"
          valor={resumen ? pesos(resumen.saldoCaja) : '…'}
          tono={resumen && resumen.saldoCaja < 0 ? 'text-red-700' : 'text-emerald-800'}
        />
        <Cifra etiqueta="Fiado por cobrar" valor={resumen ? pesos(resumen.totalPorCobrar) : '…'} tono="text-amber-700" />
        <Cifra
          etiqueta="Neto últimos 7 días"
          valor={resumen ? pesos(netoSemana) : '…'}
          tono={netoSemana < 0 ? 'text-red-700' : 'text-stone-900'}
        />
      </div>

      <div className="grid gap-6 2xl:grid-cols-2">
        <section className="space-y-2">
          <h2 className="text-lg font-semibold text-stone-900">Últimos 7 días</h2>
          <Tabla<ResumenPorDia>
            compacta
            filas={resumen?.porDia ?? []}
            columnas={columnasDeTotales<ResumenPorDia>('Día', (dia) => etiquetaDia(dia.fecha))}
            llave={(dia) => dia.fecha}
            mensajeVacio="Sin movimientos esta semana."
            cargando={!resumen}
          />
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-semibold text-stone-900">Últimos 12 meses</h2>
          <Tabla<ResumenPorMes>
            compacta
            filas={resumen?.porMes ?? []}
            columnas={columnasDeTotales<ResumenPorMes>('Mes', (mes) => etiquetaDeMes(mes.mes))}
            llave={(mes) => mes.mes}
            mensajeVacio="Sin movimientos este año."
            cargando={!resumen}
          />
        </section>
      </div>
    </>
  )
}
