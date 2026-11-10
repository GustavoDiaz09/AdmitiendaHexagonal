import { useEffect, useState } from 'react'
import { Warning } from '@phosphor-icons/react'
import { api } from '../api'
import type { Alerta as AlertaDeInventario } from '../tipos'

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
        <div className="rounded-2xl bg-white p-6 shadow-sm">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-stone-200 text-left text-stone-500">
                <th className="pb-2 font-medium">Producto</th>
                <th className="pb-2 font-medium">Tipo</th>
                <th className="pb-2 font-medium">Existencias</th>
                <th className="pb-2 font-medium">Mínimo</th>
                <th className="pb-2 font-medium">Faltante</th>
              </tr>
            </thead>
            <tbody>
              {alertas.map((a) => (
                <tr key={a.id} className="border-b border-stone-100">
                  <td className="flex items-center gap-2 py-2 text-stone-700">
                    <Warning size={16} className="text-amber-600" /> {a.nombreProducto}
                  </td>
                  <td className="py-2 text-stone-600">{a.tipoProducto}</td>
                  <td className="py-2 font-medium text-red-700">{a.cantidadStock}</td>
                  <td className="py-2 text-stone-600">{a.stockMinimo}</td>
                  <td className="py-2 text-stone-800">{a.faltante}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
