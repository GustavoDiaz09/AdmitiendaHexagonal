import { useEffect, useState } from 'react'
import { ArrowsDownUp, Plus, Trash } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { api } from '../api'
import { Tabla, type Columna } from '../componentes/Tabla'
import { aNumero, fecha, pesosExactos } from '../formato'
import type { ListadoDeMovimientos, Movimiento, TipoMovimiento } from '../tipos'

/** Caja: movimientos de ingresos y egresos con saldo histórico. */
export function Movimientos() {
  const esAdmin = useSesion((s) => s.usuario?.rol === 'ADMIN')
  const [listado, setListado] = useState<ListadoDeMovimientos | null>(null)
  const [error, setError] = useState('')
  const [abierto, setAbierto] = useState(false)
  const [tipo, setTipo] = useState<TipoMovimiento>('INGRESO')
  const [monto, setMonto] = useState('')
  const [descripcion, setDescripcion] = useState('')

  function cargar() {
    api
      .listarMovimientos()
      .then(setListado)
      .catch((e) => setError(e.message))
  }

  useEffect(cargar, [])

  async function guardar(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.crearMovimiento(tipo, aNumero(monto), descripcion)
      setAbierto(false)
      setMonto('')
      setDescripcion('')
      cargar()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo registrar.')
    }
  }

  async function eliminar(id: number) {
    if (!confirm('¿Eliminar este movimiento? El saldo de caja se recalcula.')) {
      return
    }
    try {
      await api.eliminarMovimiento(id)
      cargar()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo eliminar.')
    }
  }

  if (!listado && !error) {
    return <p className="text-stone-500">Cargando la caja…</p>
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-bold text-stone-800">Caja</h1>
        {esAdmin && (
          <button
            onClick={() => setAbierto(true)}
            className="flex items-center gap-2 rounded-lg bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
          >
            <Plus size={18} /> Registrar movimiento
          </button>
        )}
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>
      )}

      <div className="rounded-2xl bg-white p-6 shadow-sm">
        <p className="text-sm text-stone-500">Saldo en caja (histórico)</p>
        <p
          className={`mt-1 text-3xl font-bold ${
            (listado?.saldoCaja ?? 0) < 0 ? 'text-red-700' : 'text-stone-800'
          }`}
        >
          {pesosExactos(listado?.saldoCaja ?? 0)}
        </p>
      </div>

      <Tabla
        filas={listado?.movimientos ?? []}
        llave={(m) => m.id}
        columnas={[
          {
            titulo: 'Fecha',
            celda: (m) => <span className="text-stone-500">{fecha(m.fecha)}</span>,
          },
          {
            titulo: 'Tipo',
            celda: (m) => (
              <span
                className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${
                  m.tipo === 'INGRESO'
                    ? 'bg-emerald-50 text-emerald-700'
                    : 'bg-red-50 text-red-700'
                }`}
              >
                <ArrowsDownUp size={12} />
                {m.tipo === 'INGRESO' ? 'Ingreso' : 'Egreso'}
              </span>
            ),
          },
          {
            titulo: 'Descripción',
            celda: (m) => <span className="text-stone-700">{m.descripcion}</span>,
          },
          {
            titulo: 'Monto',
            celda: (m) => (
              <span
                className={`font-medium ${
                  m.tipo === 'INGRESO' ? 'text-emerald-700' : 'text-red-700'
                }`}
              >
                {m.tipo === 'INGRESO' ? '+' : '−'} {pesosExactos(m.monto)}
              </span>
            ),
          },
          ...(esAdmin
            ? [
                {
                  titulo: '',
                  celda: (m: Movimiento) => (
                    <span className="block text-right">
                      <button
                        onClick={() => eliminar(m.id)}
                        className="rounded-md px-2 py-1 text-xs font-medium text-red-700 hover:bg-red-50"
                      >
                        <Trash size={14} className="inline" />
                      </button>
                    </span>
                  ),
                } satisfies Columna<Movimiento>,
              ]
            : []),
        ]}
      />

      {abierto && (
        <form
          onSubmit={guardar}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
        >
          <div className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="text-lg font-bold text-stone-800">Registrar movimiento</h2>
            <div className="flex gap-2">
              {(['INGRESO', 'EGRESO'] as const).map((valor) => (
                <button
                  key={valor}
                  type="button"
                  onClick={() => setTipo(valor)}
                  className={`flex-1 rounded-lg py-2 text-sm font-semibold transition ${
                    tipo === valor
                      ? valor === 'INGRESO'
                        ? 'bg-emerald-600 text-white'
                        : 'bg-red-600 text-white'
                      : 'bg-stone-100 text-stone-600'
                  }`}
                >
                  {valor === 'INGRESO' ? 'Ingreso' : 'Egreso'}
                </button>
              ))}
            </div>
            <label className="block space-y-1">
              <span className="text-xs font-medium text-stone-600">Monto</span>
              <input
                value={monto}
                onChange={(e) => setMonto(e.target.value)}
                className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                inputMode="decimal"
                placeholder="0.00"
                required
              />
            </label>
            <label className="block space-y-1">
              <span className="text-xs font-medium text-stone-600">Descripción</span>
              <input
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
                className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                placeholder="Venta del día, compra de mercancía…"
                required
              />
            </label>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setAbierto(false)}
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
    </div>
  )
}
