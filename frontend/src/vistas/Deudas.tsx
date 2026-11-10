import { useEffect, useState } from 'react'
import { ArrowUUpLeft, Plus, Trash } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { api } from '../api'
import { aNumero, fecha, pesosExactos } from '../formato'
import type { DeudorResumen, HistorialDeDeudor } from '../tipos'

/** Fiado: deudores, su historial, y operaciones de deuda y abono. */
export function Deudas() {
  const esAdmin = useSesion((s) => s.usuario?.rol === 'ADMIN')
  const [deudores, setDeudores] = useState<DeudorResumen[]>([])
  const [historial, setHistorial] = useState<HistorialDeDeudor | null>(null)
  const [error, setError] = useState('')
  const [nuevaDeuda, setNuevaDeuda] = useState(false)
  const [formaDeuda, setFormaDeuda] = useState({ nombreDeudor: '', monto: '', descripcion: '' })
  const [formaAbono, setFormaAbono] = useState({ id: 0, monto: '', descripcion: '' })

  function cargarDeudores() {
    api
      .listarDeudores()
      .then(setDeudores)
      .catch((e) => setError(e.message))
  }

  function abrirHistorial(id: number) {
    setError('')
    api
      .historialDeDeudor(id)
      .then(setHistorial)
      .catch((e) => setError(e.message))
  }

  useEffect(cargarDeudores, [])

  async function guardarDeuda(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.registrarDeuda(
        formaDeuda.nombreDeudor,
        aNumero(formaDeuda.monto),
        formaDeuda.descripcion,
      )
      setNuevaDeuda(false)
      setFormaDeuda({ nombreDeudor: '', monto: '', descripcion: '' })
      cargarDeudores()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo registrar la deuda.')
    }
  }

  async function abonar(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.abonarDeuda(formaAbono.id, aNumero(formaAbono.monto), formaAbono.descripcion)
      setFormaAbono({ id: 0, monto: '', descripcion: '' })
      if (historial) abrirHistorial(historial.id)
      cargarDeudores()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo registrar el abono.')
    }
  }

  async function eliminarDeuda(id: number) {
    if (!confirm('¿Eliminar la deuda y todos sus abonos?')) {
      return
    }
    try {
      await api.eliminarDeuda(id)
      if (historial) abrirHistorial(historial.id)
      cargarDeudores()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo eliminar.')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-bold text-stone-800">Fiado</h1>
        {esAdmin && (
          <button
            onClick={() => setNuevaDeuda(true)}
            className="flex items-center gap-2 rounded-lg bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
          >
            <Plus size={18} /> Nueva venta fiada
          </button>
        )}
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>
      )}

      <div className="overflow-x-auto rounded-2xl bg-white p-6 shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-stone-200 text-left text-stone-500">
              <th className="pb-2 font-medium">Deudor</th>
              <th className="pb-2 font-medium">Deudas pendientes</th>
              <th className="pb-2 font-medium">Total por cobrar</th>
              <th className="pb-2 font-medium" />
            </tr>
          </thead>
          <tbody>
            {deudores.map((d) => (
              <tr key={d.id} className="border-b border-stone-100">
                <td className="py-2 font-medium text-stone-700">{d.nombreDeudor}</td>
                <td className="py-2 text-stone-600">{d.deudasPendientes}</td>
                <td className="py-2 text-amber-700">{pesosExactos(d.totalPendiente)}</td>
                <td className="py-2 text-right">
                  <button
                    onClick={() => abrirHistorial(d.id)}
                    className="rounded-md px-2 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-50"
                  >
                    Ver historial
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {historial && (
        <section className="space-y-4 rounded-2xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-stone-700">
              Historial de {historial.nombreDeudor}
            </h2>
            <button
              onClick={() => setHistorial(null)}
              className="text-sm text-stone-500 hover:underline"
            >
              Cerrar
            </button>
          </div>

          {historial.deudas.map((deuda) => (
            <article key={deuda.id} className="rounded-xl border border-stone-200 p-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <p className="font-medium text-stone-800">
                    {pesosExactos(deuda.monto)}
                    {!deuda.saldo && (
                      <span className="ml-2 rounded-full bg-emerald-100 px-2 py-0.5 text-xs text-emerald-800">
                        saldada
                      </span>
                    )}
                  </p>
                  <p className="text-sm text-stone-500">
                    {deuda.descripcion} · {fecha(deuda.fecha)}
                  </p>
                </div>
                {esAdmin && deuda.saldo > 0 && (
                  <div className="flex gap-2">
                    <button
                      onClick={() =>
                        setFormaAbono({ id: deuda.id, monto: '', descripcion: '' })
                      }
                      className="flex items-center gap-1 rounded-md bg-emerald-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-800"
                    >
                      <ArrowUUpLeft size={14} /> Abonar
                    </button>
                    <button
                      onClick={() => eliminarDeuda(deuda.id)}
                      className="rounded-md px-2 py-1.5 text-xs font-medium text-red-700 hover:bg-red-50"
                    >
                      <Trash size={14} className="inline" /> Eliminar
                    </button>
                  </div>
                )}
                {esAdmin && deuda.saldo === 0 && (
                  <button
                    onClick={() => eliminarDeuda(deuda.id)}
                    className="rounded-md px-2 py-1.5 text-xs font-medium text-red-700 hover:bg-red-50"
                  >
                    <Trash size={14} className="inline" /> Eliminar
                  </button>
                )}
              </div>
              <p className="mt-2 text-sm">
                Saldo:{' '}
                <span className="font-semibold text-amber-700">
                  {pesosExactos(deuda.saldo)}
                </span>
              </p>
              {deuda.pagos.length > 0 && (
                <ul className="mt-2 space-y-1 border-l-2 border-stone-200 pl-3 text-sm text-stone-500">
                  {deuda.pagos.map((pago) => (
                    <li key={pago.id}>
                      {pesosExactos(pago.monto)} · {fecha(pago.fecha)}
                    </li>
                  ))}
                </ul>
              )}
            </article>
          ))}
        </section>
      )}

      {nuevaDeuda && (
        <form
          onSubmit={guardarDeuda}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
        >
          <div className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="text-lg font-bold text-stone-800">Nueva venta fiada</h2>
            <label className="block space-y-1">
              <span className="text-xs font-medium text-stone-600">Nombre del deudor</span>
              <input
                value={formaDeuda.nombreDeudor}
                onChange={(e) =>
                  setFormaDeuda({ ...formaDeuda, nombreDeudor: e.target.value })
                }
                className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                placeholder="María Pérez"
                required
              />
            </label>
            <label className="block space-y-1">
              <span className="text-xs font-medium text-stone-600">Monto</span>
              <input
                value={formaDeuda.monto}
                onChange={(e) => setFormaDeuda({ ...formaDeuda, monto: e.target.value })}
                className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                inputMode="decimal"
                required
              />
            </label>
            <label className="block space-y-1">
              <span className="text-xs font-medium text-stone-600">Descripción</span>
              <input
                value={formaDeuda.descripcion}
                onChange={(e) =>
                  setFormaDeuda({ ...formaDeuda, descripcion: e.target.value })
                }
                className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                placeholder="Mercado fiado…"
                required
              />
            </label>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setNuevaDeuda(false)}
                className="rounded-lg px-4 py-2 text-sm font-medium text-stone-600 hover:bg-stone-100"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="rounded-lg bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
              >
                Guardar
              </button>
            </div>
          </div>
        </form>
      )}

      {formaAbono.id > 0 && (
        <form
          onSubmit={abonar}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
        >
          <div className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="text-lg font-bold text-stone-800">Abonar a la deuda</h2>
            <label className="block space-y-1">
              <span className="text-xs font-medium text-stone-600">Monto del abono</span>
              <input
                value={formaAbono.monto}
                onChange={(e) => setFormaAbono({ ...formaAbono, monto: e.target.value })}
                className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                inputMode="decimal"
                required
                autoFocus
              />
            </label>
            <label className="block space-y-1">
              <span className="text-xs font-medium text-stone-600">Detalle (opcional)</span>
              <input
                value={formaAbono.descripcion}
                onChange={(e) =>
                  setFormaAbono({ ...formaAbono, descripcion: e.target.value })
                }
                className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                placeholder="Abono parcial…"
              />
            </label>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setFormaAbono({ id: 0, monto: '', descripcion: '' })}
                className="rounded-lg px-4 py-2 text-sm font-medium text-stone-600 hover:bg-stone-100"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="rounded-lg bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
              >
                Abonar
              </button>
            </div>
          </div>
        </form>
      )}
    </div>
  )
}
