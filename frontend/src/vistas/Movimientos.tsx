import { useEffect, useState } from 'react'
import { Plus, Trash } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { api } from '../api'
import { Tabla, type Columna } from '../componentes/Tabla'
import { Aviso, Boton, Cabecera, Campo, Estadistica, Insignia, Modal } from '../componentes/ui'
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

  const columnas: Columna<Movimiento>[] = [
    {
      titulo: 'Fecha',
      celda: (m) => <span className="text-stone-500">{fecha(m.fecha)}</span>,
    },
    {
      titulo: 'Tipo',
      celda: (m) =>
        m.tipo === 'INGRESO' ? <Insignia tono="verde">Ingreso</Insignia> : <Insignia tono="rojo">Egreso</Insignia>,
    },
    {
      titulo: 'Descripción',
      celda: (m) => <span className="text-stone-800">{m.descripcion}</span>,
    },
    {
      titulo: 'Monto',
      derecha: true,
      celda: (m) => (
        <span className={`font-semibold tabular-nums ${m.tipo === 'INGRESO' ? 'text-emerald-800' : 'text-red-700'}`}>
          {m.tipo === 'INGRESO' ? '+' : '−'} {pesosExactos(m.monto)}
        </span>
      ),
    },
  ]

  if (esAdmin) {
    columnas.push({
      titulo: '',
      derecha: true,
      celda: (m) => (
        <button
          type="button"
          onClick={() => eliminar(m.id)}
          aria-label={`Eliminar movimiento: ${m.descripcion}`}
          className="rounded-md p-2 text-stone-500 hover:bg-red-50 hover:text-red-700"
        >
          <Trash size={16} />
        </button>
      ),
    })
  }

  const saldo = listado?.saldoCaja ?? 0

  return (
    <>
      <Cabecera
        titulo="Caja"
        subtitulo="Ingresos y egresos de la tienda."
        acciones={
          esAdmin && (
            <Boton onClick={() => setAbierto(true)}>
              <Plus size={16} weight="bold" /> Registrar movimiento
            </Boton>
          )
        }
      />

      {error && <Aviso tono="error">{error}</Aviso>}

      <Estadistica
        etiqueta="Saldo en caja (histórico)"
        valor={pesosExactos(saldo)}
        tono={saldo < 0 ? 'negativo' : 'positivo'}
      />

      <Tabla
        filas={listado?.movimientos ?? []}
        llave={(m) => m.id}
        columnas={columnas}
        mensajeVacio="Aún no hay movimientos."
        cargando={!listado && !error}
      />

      {abierto && (
        <Modal titulo="Registrar movimiento" onCerrar={() => setAbierto(false)}>
          <form onSubmit={guardar} className="space-y-5">
            <div role="group" aria-label="Tipo de movimiento" className="grid grid-cols-2 gap-2 rounded-xl bg-stone-100 p-1">
              {(['INGRESO', 'EGRESO'] as const).map((valor) => (
                <button
                  key={valor}
                  type="button"
                  aria-pressed={tipo === valor}
                  onClick={() => setTipo(valor)}
                  className={`rounded-lg py-2 text-sm font-medium transition ${
                    tipo === valor
                      ? valor === 'INGRESO'
                        ? 'bg-white text-emerald-800 shadow-sm'
                        : 'bg-white text-red-700 shadow-sm'
                      : 'text-stone-500 hover:text-stone-700'
                  }`}
                >
                  {valor === 'INGRESO' ? 'Ingreso' : 'Egreso'}
                </button>
              ))}
            </div>
            <Campo
              etiqueta="Monto"
              value={monto}
              onChange={(e) => setMonto(e.target.value)}
              inputMode="decimal"
              placeholder="15.000"
              ayuda="Puedes usar punto para miles y coma para decimales."
              required
              autoFocus
            />
            <Campo
              etiqueta="Descripción"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              placeholder="Venta del día, compra de mercancía…"
              required
            />
            <div className="flex justify-end gap-2 pt-2">
              <Boton variante="fantasma" onClick={() => setAbierto(false)}>
                Cancelar
              </Boton>
              <Boton type="submit">Guardar</Boton>
            </div>
          </form>
        </Modal>
      )}
    </>
  )
}
