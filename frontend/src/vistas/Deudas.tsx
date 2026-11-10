import { useEffect, useState } from 'react'
import { Plus, Trash, X } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { api } from '../api'
import { Tabla, type Columna } from '../componentes/Tabla'
import { Aviso, Boton, Cabecera, Campo, Insignia, Modal, Tarjeta } from '../componentes/ui'
import { aNumero, fecha, pesosExactos } from '../formato'
import type { DeudorResumen, HistorialDeDeudor } from '../tipos'

/** Fiado: deudores, su historial, y operaciones de deuda y abono. */
export function Deudas() {
  const esAdmin = useSesion((s) => s.usuario?.rol === 'ADMIN')
  const [deudores, setDeudores] = useState<DeudorResumen[]>([])
  const [cargando, setCargando] = useState(true)
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
      .finally(() => setCargando(false))
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
      await api.registrarDeuda(formaDeuda.nombreDeudor, aNumero(formaDeuda.monto), formaDeuda.descripcion)
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

  const columnas: Columna<DeudorResumen>[] = [
    {
      titulo: 'Deudor',
      celda: (d) => <span className="font-medium text-stone-800">{d.nombreDeudor}</span>,
    },
    {
      titulo: 'Deudas pendientes',
      celda: (d) => <span className="tabular-nums text-stone-600">{d.deudasPendientes}</span>,
    },
    {
      titulo: 'Total por cobrar',
      derecha: true,
      celda: (d) => <span className="font-semibold tabular-nums text-amber-700">{pesosExactos(d.totalPendiente)}</span>,
    },
    {
      titulo: '',
      derecha: true,
      celda: (d) => (
        <Boton variante="fantasma" onClick={() => abrirHistorial(d.id)} className="px-3 py-1.5 text-emerald-800">
          Ver historial
        </Boton>
      ),
    },
  ]

  return (
    <>
      <Cabecera
        titulo="Fiado"
        subtitulo="Lo que los clientes deben a la tienda."
        acciones={
          esAdmin && (
            <Boton onClick={() => setNuevaDeuda(true)}>
              <Plus size={16} weight="bold" /> Nueva venta fiada
            </Boton>
          )
        }
      />

      {error && <Aviso tono="error">{error}</Aviso>}

      <Tabla
        filas={deudores}
        llave={(d) => d.id}
        columnas={columnas}
        mensajeVacio="Nadie tiene fiado pendiente."
        cargando={cargando}
      />

      {historial && (
        <Tarjeta className="space-y-5">
          <div className="flex items-center justify-between gap-4">
            <h2 className="text-lg font-semibold text-stone-900">Historial de {historial.nombreDeudor}</h2>
            <button
              type="button"
              onClick={() => setHistorial(null)}
              aria-label="Cerrar historial"
              className="rounded-md p-1.5 text-stone-500 hover:bg-stone-100"
            >
              <X size={16} />
            </button>
          </div>

          {historial.deudas.length === 0 && <p className="text-sm text-stone-500">Sin deudas registradas.</p>}

          {historial.deudas.map((deuda) => (
            <article key={deuda.id} className="rounded-xl border border-stone-200 p-5">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="flex flex-wrap items-center gap-2 text-base font-semibold tabular-nums text-stone-900">
                    {pesosExactos(deuda.monto)}
                    {deuda.saldo === 0 && <Insignia tono="verde">Saldada</Insignia>}
                  </p>
                  <p className="mt-0.5 text-sm text-stone-500">
                    {deuda.descripcion} · {fecha(deuda.fecha)}
                  </p>
                </div>
                <div className="flex items-center gap-1">
                  {esAdmin && deuda.saldo > 0 && (
                    <Boton
                      onClick={() => setFormaAbono({ id: deuda.id, monto: '', descripcion: '' })}
                      className="px-3 py-1.5 text-xs"
                    >
                      Abonar
                    </Boton>
                  )}
                  {esAdmin && (
                    <button
                      type="button"
                      onClick={() => eliminarDeuda(deuda.id)}
                      aria-label="Eliminar deuda"
                      className="rounded-md p-2 text-stone-500 hover:bg-red-50 hover:text-red-700"
                    >
                      <Trash size={16} />
                    </button>
                  )}
                </div>
              </div>

              <p className="mt-3 text-sm text-stone-600">
                Saldo pendiente:{' '}
                <span className="font-semibold tabular-nums text-amber-700">{pesosExactos(deuda.saldo)}</span>
              </p>

              {deuda.pagos.length > 0 && (
                <ul className="mt-3 space-y-1.5 border-l-2 border-emerald-200 pl-4 text-sm text-stone-600">
                  {deuda.pagos.map((pago) => (
                    <li key={pago.id} className="tabular-nums">
                      Abono de {pesosExactos(pago.monto)} · {fecha(pago.fecha)}
                    </li>
                  ))}
                </ul>
              )}
            </article>
          ))}
        </Tarjeta>
      )}

      {nuevaDeuda && (
        <Modal titulo="Nueva venta fiada" onCerrar={() => setNuevaDeuda(false)}>
          <form onSubmit={guardarDeuda} className="space-y-4">
            <Campo
              etiqueta="Nombre del deudor"
              value={formaDeuda.nombreDeudor}
              onChange={(e) => setFormaDeuda({ ...formaDeuda, nombreDeudor: e.target.value })}
              placeholder="María Pérez"
              required
              autoFocus
            />
            <Campo
              etiqueta="Monto"
              value={formaDeuda.monto}
              onChange={(e) => setFormaDeuda({ ...formaDeuda, monto: e.target.value })}
              inputMode="decimal"
              required
            />
            <Campo
              etiqueta="Descripción"
              value={formaDeuda.descripcion}
              onChange={(e) => setFormaDeuda({ ...formaDeuda, descripcion: e.target.value })}
              placeholder="Mercado fiado…"
              required
            />
            <div className="flex justify-end gap-2 pt-2">
              <Boton variante="fantasma" onClick={() => setNuevaDeuda(false)}>
                Cancelar
              </Boton>
              <Boton type="submit">Guardar</Boton>
            </div>
          </form>
        </Modal>
      )}

      {formaAbono.id > 0 && (
        <Modal titulo="Abonar a la deuda" onCerrar={() => setFormaAbono({ id: 0, monto: '', descripcion: '' })}>
          <form onSubmit={abonar} className="space-y-4">
            <Campo
              etiqueta="Monto del abono"
              value={formaAbono.monto}
              onChange={(e) => setFormaAbono({ ...formaAbono, monto: e.target.value })}
              inputMode="decimal"
              required
              autoFocus
            />
            <Campo
              etiqueta="Detalle (opcional)"
              value={formaAbono.descripcion}
              onChange={(e) => setFormaAbono({ ...formaAbono, descripcion: e.target.value })}
              placeholder="Abono parcial…"
            />
            <div className="flex justify-end gap-2 pt-2">
              <Boton variante="fantasma" onClick={() => setFormaAbono({ id: 0, monto: '', descripcion: '' })}>
                Cancelar
              </Boton>
              <Boton type="submit">Abonar</Boton>
            </div>
          </form>
        </Modal>
      )}
    </>
  )
}
