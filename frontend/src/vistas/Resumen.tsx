import { useEffect, useState } from 'react'
import { api } from '../api'
import { Aviso, Cabecera, Estadistica } from '../componentes/ui'
import { Tabla, type Columna } from '../componentes/Tabla'
import { etiquetaDeMes, etiquetaDia, pesos } from '../formato'
import type { Resumen as ResumenDeTienda, ResumenPorDia, ResumenPorMes } from '../tipos'

/** Columnas compartidas por las tablas de días y de meses. */
function columnasDeTotales<T extends { ingresos: number; egresos: number; neto: number }>(
  titulo: string,
  etiqueta: (fila: T) => string,
): Columna<T>[] {
  return [
    { titulo, celda: (fila) => <span className="font-medium text-stone-800">{etiqueta(fila)}</span> },
    {
      titulo: 'Ingresos',
      derecha: true,
      celda: (fila) => <span className="tabular-nums text-emerald-800">{pesos(fila.ingresos)}</span>,
    },
    {
      titulo: 'Egresos',
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
    return <Aviso tono="error">{error}</Aviso>
  }

  return (
    <>
      <Cabecera titulo="Resumen" subtitulo="Cómo va la tienda hoy." />

      {resumen ? (
        <div className="grid gap-4 sm:grid-cols-2">
          <Estadistica
            etiqueta="Saldo en caja"
            valor={pesos(resumen.saldoCaja)}
            tono={resumen.saldoCaja < 0 ? 'negativo' : 'positivo'}
          />
          <Estadistica etiqueta="Fiado por cobrar" valor={pesos(resumen.totalPorCobrar)} tono="alerta" />
        </div>
      ) : (
        <p className="text-sm text-stone-500">Cargando el resumen…</p>
      )}

      <section className="space-y-3">
        <h2 className="text-sm font-semibold uppercase tracking-wider text-stone-500">Últimos 7 días</h2>
        <Tabla<ResumenPorDia>
          filas={resumen?.porDia ?? []}
          columnas={columnasDeTotales<ResumenPorDia>('Día', (dia) => etiquetaDia(dia.fecha))}
          llave={(dia) => dia.fecha}
          mensajeVacio="Sin movimientos esta semana."
          cargando={!resumen}
        />
      </section>

      <section className="space-y-3">
        <h2 className="text-sm font-semibold uppercase tracking-wider text-stone-500">Últimos 12 meses</h2>
        <Tabla<ResumenPorMes>
          filas={resumen?.porMes ?? []}
          columnas={columnasDeTotales<ResumenPorMes>('Mes', (mes) => etiquetaDeMes(mes.mes))}
          llave={(mes) => mes.mes}
          mensajeVacio="Sin movimientos este año."
          cargando={!resumen}
        />
      </section>
    </>
  )
}
