import { useEffect, useState } from 'react'
import { Warning } from '@phosphor-icons/react'
import { api } from '../api'
import { Tabla, type Columna } from '../componentes/Tabla'
import type { Alerta as AlertaDeInventario } from '../tipos'

const COLUMNAS: Columna<AlertaDeInventario>[] = [
  {
    titulo: 'Producto',
    celda: (a) => (
      <span className="flex items-center gap-2 text-stone-700">
        <Warning size={16} className="text-amber-600" /> {a.nombreProducto}
      </span>
    ),
  },
  { titulo: 'Tipo', celda: (a) => <span className="text-stone-600">{a.tipoProducto}</span> },
  {
    titulo: 'Existencias',
    celda: (a) => <span className="font-medium text-red-700">{a.cantidadStock}</span>,
  },
  { titulo: 'Mínimo', celda: (a) => <span className="text-stone-600">{a.stockMinimo}</span> },
  {
    titulo: 'Faltante',
    celda: (a) => <span className="text-stone-800">{a.faltante}</span>,
  },
]

/** Productos con existencias al mínimo o menos. */
export function Alertas() {
  const [alertas, setAlertas] = useState<AlertaDeInventario[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .alertas()
      .then(setAlertas)
      .catch((e) => setError(e.message))
  }, [])

  if (error) {
    return <p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-stone-800">Alertas de inventario</h1>

      {alertas.length === 0 ? (
        <p className="rounded-2xl bg-white p-6 text-stone-500 shadow-sm">
          Todo el inventario está por encima del mínimo.
        </p>
      ) : (
        <Tabla
          filas={alertas}
          columnas={COLUMNAS}
          llave={(a) => a.id}
        />
      )}
    </div>
  )
}
