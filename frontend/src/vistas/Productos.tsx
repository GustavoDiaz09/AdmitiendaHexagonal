import { useEffect, useState } from 'react'
import { MagnifyingGlass, PencilSimple, Plus, Trash } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { api } from '../api'
import { Tabla, type Columna } from '../componentes/Tabla'
import { Aviso, Boton, Cabecera, Campo, Insignia, Modal } from '../componentes/ui'
import { aNumero, pesosExactos } from '../formato'
import type { Producto } from '../tipos'

interface Formulario {
  tipoProducto: string
  nombreProducto: string
  precioNeto: string
  ganancia: string
  cantidadStock: string
  stockMinimo: string
}

const FORMULARIO_VACIO: Formulario = {
  tipoProducto: '',
  nombreProducto: '',
  precioNeto: '',
  ganancia: '',
  cantidadStock: '',
  stockMinimo: '',
}

/** Catálogo de productos con búsqueda y CRUD para administradores. */
export function Productos() {
  const esAdmin = useSesion((s) => s.usuario?.rol === 'ADMIN')
  const [productos, setProductos] = useState<Producto[]>([])
  const [busqueda, setBusqueda] = useState('')
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [editando, setEditando] = useState<Producto | null>(null)
  const [formulario, setFormulario] = useState<Formulario | null>(null)

  function cargar(filtro = '') {
    setCargando(true)
    api
      .listarProductos(filtro || undefined)
      .then(setProductos)
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false))
  }

  useEffect(() => {
    cargar()
  }, [])

  function abrirNuevo() {
    setEditando(null)
    setFormulario(FORMULARIO_VACIO)
  }

  function abrirEdicion(producto: Producto) {
    setEditando(producto)
    setFormulario({
      tipoProducto: producto.tipoProducto,
      nombreProducto: producto.nombreProducto,
      precioNeto: String(producto.precioNeto),
      ganancia: String(producto.ganancia),
      cantidadStock: String(producto.cantidadStock),
      stockMinimo: String(producto.stockMinimo),
    })
  }

  async function guardar(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    const datos = {
      tipoProducto: formulario!.tipoProducto,
      nombreProducto: formulario!.nombreProducto,
      precioNeto: aNumero(formulario!.precioNeto),
      ganancia: aNumero(formulario!.ganancia),
      cantidadStock: Number(formulario!.cantidadStock),
      stockMinimo: Number(formulario!.stockMinimo),
    }
    try {
      if (editando) {
        await api.editarProducto(editando.id, datos)
      } else {
        await api.crearProducto(datos)
      }
      setFormulario(null)
      cargar(busqueda)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo guardar.')
    }
  }

  async function eliminar(producto: Producto) {
    if (!confirm(`¿Eliminar "${producto.nombreProducto}" del catálogo?`)) {
      return
    }
    try {
      await api.eliminarProducto(producto.id)
      cargar(busqueda)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo eliminar.')
    }
  }

  const columnas: Columna<Producto>[] = [
    {
      titulo: 'Producto',
      celda: (p) => (
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-medium text-stone-800">{p.nombreProducto}</span>
          {p.enAlerta && <Insignia tono="ambar">Stock bajo</Insignia>}
        </div>
      ),
    },
    { titulo: 'Tipo', celda: (p) => <span className="text-stone-600">{p.tipoProducto}</span> },
    {
      titulo: 'Precio de venta',
      derecha: true,
      celda: (p) => <span className="tabular-nums text-stone-900">{pesosExactos(p.precioVenta)}</span>,
    },
    {
      titulo: 'Existencias',
      derecha: true,
      celda: (p) => (
        <span className="tabular-nums text-stone-600">
          {p.cantidadStock} <span className="text-stone-400">/ mín. {p.stockMinimo}</span>
        </span>
      ),
    },
  ]

  if (esAdmin) {
    columnas.push({
      titulo: 'Acciones',
      derecha: true,
      celda: (p) => (
        <span className="inline-flex gap-1">
          <button
            type="button"
            onClick={() => abrirEdicion(p)}
            aria-label={`Editar ${p.nombreProducto}`}
            className="rounded-md p-2 text-stone-500 hover:bg-emerald-50 hover:text-emerald-800"
          >
            <PencilSimple size={16} />
          </button>
          <button
            type="button"
            onClick={() => eliminar(p)}
            aria-label={`Eliminar ${p.nombreProducto}`}
            className="rounded-md p-2 text-stone-500 hover:bg-red-50 hover:text-red-700"
          >
            <Trash size={16} />
          </button>
        </span>
      ),
    })
  }

  return (
    <>
      <Cabecera
        titulo="Productos"
        subtitulo="Catálogo, precios y existencias."
        acciones={
          esAdmin && (
            <Boton onClick={abrirNuevo}>
              <Plus size={16} weight="bold" /> Nuevo producto
            </Boton>
          )
        }
      />

      {error && <Aviso tono="error">{error}</Aviso>}

      <form
        role="search"
        onSubmit={(e) => {
          e.preventDefault()
          cargar(busqueda)
        }}
        className="flex max-w-md gap-2"
      >
        <label className="relative w-full">
          <span className="sr-only">Buscar producto por nombre</span>
          <MagnifyingGlass
            size={16}
            className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-stone-400"
          />
          <input
            type="search"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar por nombre…"
            className="w-full rounded-lg border border-stone-300 bg-white py-2 pl-9 pr-3 text-sm placeholder:text-stone-400 focus:border-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
          />
        </label>
        <Boton variante="secundario">Buscar</Boton>
      </form>

      <Tabla
        filas={productos}
        llave={(p) => p.id}
        columnas={columnas}
        mensajeVacio="No hay productos que coincidan."
        cargando={cargando}
      />

      {formulario && (
        <Modal titulo={editando ? 'Editar producto' : 'Nuevo producto'} onCerrar={() => setFormulario(null)}>
          <form onSubmit={guardar} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="col-span-2">
                <Campo
                  etiqueta="Nombre"
                  value={formulario.nombreProducto}
                  onChange={(e) => setFormulario({ ...formulario, nombreProducto: e.target.value })}
                  required
                  autoFocus
                />
              </div>
              <div className="col-span-2">
                <Campo
                  etiqueta="Tipo"
                  value={formulario.tipoProducto}
                  onChange={(e) => setFormulario({ ...formulario, tipoProducto: e.target.value })}
                  placeholder="Abarrotes, Dulces…"
                  required
                />
              </div>
              <Campo
                etiqueta="Precio neto"
                value={formulario.precioNeto}
                onChange={(e) => setFormulario({ ...formulario, precioNeto: e.target.value })}
                inputMode="decimal"
                required
              />
              <Campo
                etiqueta="Ganancia"
                value={formulario.ganancia}
                onChange={(e) => setFormulario({ ...formulario, ganancia: e.target.value })}
                inputMode="decimal"
                required
              />
              <Campo
                etiqueta="Existencias"
                value={formulario.cantidadStock}
                onChange={(e) => setFormulario({ ...formulario, cantidadStock: e.target.value })}
                inputMode="numeric"
                required
              />
              <Campo
                etiqueta="Mínimo"
                value={formulario.stockMinimo}
                onChange={(e) => setFormulario({ ...formulario, stockMinimo: e.target.value })}
                inputMode="numeric"
                required
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Boton variante="fantasma" onClick={() => setFormulario(null)}>
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
