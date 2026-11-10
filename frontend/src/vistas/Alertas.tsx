import { useEffect, useState } from 'react'
import { CheckCircle, Warning } from '@phosphor-icons/react'
import { api } from '../api'
import { Tabla, type Columna } from '../componentes/Tabla'
import { Aviso, Cabecera, Insignia, Tarjeta } from '../componentes/ui'
import type { Alerta as AlertaDeInventario } from '../tipos'

const COLUMNAS: Columna<AlertaDeInventario>[] = [
  {
    titulo: 'Producto',
    celda: (a) => <span className="font-medium text-stone-800">{a.nombreProducto}</span>,
  },
  { titulo: 'Tipo', celda: (a) => <span className="text-stone-600">{a.tipoProducto}</span> },
  {
    titulo: 'Existencias',
    derecha: true,
    celda: (a) => (
      <Insignia tono="rojo">
        <Warning size={12} weight="fill" /> {a.cantidadStock}
      </Insignia>
    ),
  },
  {
    titulo: 'Mínimo',
    derecha: true,
    celda: (a) => <span className="tabular-nums text-stone-600">{a.stockMinimo}</span>,
  },
  {
    titulo: 'Faltante',
    derecha: true,
    celda: (a) => <span className="font-semibold tabular-nums text-stone-900">{a.faltante}</span>,
  },
]

/** Productos con existencias al mínimo o menos. */
export function Alertas() {
  const [alertas, setAlertas] = useState<AlertaDeInventario[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .alertas()
      .then(setAlertas)
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false))
  }, [])

  return (
    <>
      <Cabecera
        titulo="Alertas de inventario"
        subtitulo={
          cargando
            ? 'Revisando existencias…'
            : `${alertas.length} producto${alertas.length === 1 ? '' : 's'} al mínimo o por debajo.`
        }
      />

      {error && <Aviso tono="error">{error}</Aviso>}

      {!cargando && !error && alertas.length === 0 ? (
        <Tarjeta className="flex items-center gap-4">
          <span className="flex size-10 items-center justify-center rounded-full bg-emerald-50 text-emerald-700">
            <CheckCircle size={22} weight="fill" />
          </span>
          <div>
            <p className="font-medium text-stone-900">Inventario en orden</p>
            <p className="text-sm text-stone-500">Todo está por encima del mínimo.</p>
          </div>
        </Tarjeta>
      ) : (
        <Tabla
          filas={alertas}
          columnas={COLUMNAS}
          llave={(a) => a.id}
          cargando={cargando}
        />
      )}
    </>
  )
}
