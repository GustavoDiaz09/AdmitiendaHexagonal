import { useEffect, useState } from 'react'
import { api } from '../api'
import type { Resumen as ResumenDeTienda } from '../tipos'
import { pesos } from '../formato'

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

      <section className="rounded-2xl bg-white p-6 shadow-sm">
        <h2 className="mb-4 font-semibold text-stone-700">Últimos 7 días</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-stone-200 text-left text-stone-500">
              <th className="pb-2 font-medium">Día</th>
              <th className="pb-2 font-medium">Ingresos</th>
              <th className="pb-2 font-medium">Egresos</th>
              <th className="pb-2 font-medium">Neto</th>
            </tr>
          </thead>
          <tbody>
            {resumen.porDia.map((dia) => (
              <tr key={dia.fecha} className="border-b border-stone-100">
                <td className="py-2 text-stone-700">{dia.fecha}</td>
                <td className="py-2 text-emerald-700">{pesos(dia.ingresos)}</td>
                <td className="py-2 text-red-700">{pesos(dia.egresos)}</td>
                <td
                  className={`py-2 font-medium ${dia.neto < 0 ? 'text-red-700' : 'text-stone-800'}`}
                >
                  {pesos(dia.neto)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="rounded-2xl bg-white p-6 shadow-sm">
        <h2 className="mb-4 font-semibold text-stone-700">Últimos 12 meses</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-stone-200 text-left text-stone-500">
              <th className="pb-2 font-medium">Mes</th>
              <th className="pb-2 font-medium">Ingresos</th>
              <th className="pb-2 font-medium">Egresos</th>
              <th className="pb-2 font-medium">Neto</th>
            </tr>
          </thead>
          <tbody>
            {resumen.porMes.map((mes) => (
              <tr key={mes.mes} className="border-b border-stone-100">
                <td className="py-2 text-stone-700">{mes.mes}</td>
                <td className="py-2 text-emerald-700">{pesos(mes.ingresos)}</td>
                <td className="py-2 text-red-700">{pesos(mes.egresos)}</td>
                <td
                  className={`py-2 font-medium ${mes.neto < 0 ? 'text-red-700' : 'text-stone-800'}`}
                >
                  {pesos(mes.neto)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}
