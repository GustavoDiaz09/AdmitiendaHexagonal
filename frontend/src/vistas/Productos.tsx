import { useEffect, useState } from 'react'
import { MagnifyingGlass, Plus, Trash } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { api } from '../api'
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
  const [error, setError] = useState('')
  const [editando, setEditando] = useState<Producto | null>(null)
  const [formulario, setFormulario] = useState<Formulario | null>(null)

  function cargar(filtro = '') {
    api
      .listarProductos(filtro || undefined)
      .then(setProductos)
      .catch((e) => setError(e.message))
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

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-bold text-stone-800">Productos</h1>
        {esAdmin && (
          <button
            onClick={abrirNuevo}
            className="flex items-center gap-2 rounded-lg bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
          >
            <Plus size={18} /> Nuevo producto
          </button>
        )}
      </div>

      {error && (
        <p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>
      )}

      <form
        onSubmit={(e) => {
          e.preventDefault()
          cargar(busqueda)
        }}
        className="flex max-w-md gap-2"
      >
        <input
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          placeholder="Buscar por nombre…"
          className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm focus:border-emerald-600 focus:outline-none"
        />
        <button className="flex items-center gap-1 rounded-lg bg-stone-200 px-3 py-2 text-sm font-medium text-stone-700 hover:bg-stone-300">
          <MagnifyingGlass size={16} /> Buscar
        </button>
      </form>

      <div className="overflow-x-auto rounded-2xl bg-white p-6 shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-stone-200 text-left text-stone-500">
              <th className="pb-2 font-medium">Producto</th>
              <th className="pb-2 font-medium">Tipo</th>
              <th className="pb-2 font-medium">Precio de venta</th>
              <th className="pb-2 font-medium">Existencias</th>
              {esAdmin && <th className="pb-2 font-medium">Acciones</th>}
            </tr>
          </thead>
          <tbody>
            {productos.map((p) => (
              <tr key={p.id} className="border-b border-stone-100">
                <td className="py-2 text-stone-700">
                  {p.nombreProducto}
                  {p.enAlerta && (
                    <span className="ml-2 rounded-full bg-amber-100 px-2 py-0.5 text-xs text-amber-800">
                      stock bajo
                    </span>
                  )}
                </td>
                <td className="py-2 text-stone-600">{p.tipoProducto}</td>
                <td className="py-2 text-stone-800">{pesosExactos(p.precioVenta)}</td>
                <td className="py-2 text-stone-600">
                  {p.cantidadStock} / mín. {p.stockMinimo}
                </td>
                {esAdmin && (
                  <td className="space-x-2 py-2">
                    <button
                      onClick={() => abrirEdicion(p)}
                      className="rounded-md px-2 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-50"
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => eliminar(p)}
                      className="flex-inline items-center rounded-md px-2 py-1 text-xs font-medium text-red-700 hover:bg-red-50"
                    >
                      <Trash size={14} className="inline" /> Eliminar
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        {productos.length === 0 && (
          <p className="py-6 text-center text-stone-400">Sin resultados.</p>
        )}
      </div>

      {formulario && (
        <form
          onSubmit={guardar}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
        >
          <div className="w-full max-w-md space-y-4 rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="text-lg font-bold text-stone-800">
              {editando ? 'Editar producto' : 'Nuevo producto'}
            </h2>
            <div className="grid grid-cols-2 gap-3">
              <label className="col-span-2 space-y-1">
                <span className="text-xs font-medium text-stone-600">Nombre</span>
                <input
                  value={formulario.nombreProducto}
                  onChange={(e) =>
                    setFormulario({ ...formulario, nombreProducto: e.target.value })
                  }
                  className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                  required
                />
              </label>
              <label className="col-span-2 space-y-1">
                <span className="text-xs font-medium text-stone-600">Tipo</span>
                <input
                  value={formulario.tipoProducto}
                  onChange={(e) =>
                    setFormulario({ ...formulario, tipoProducto: e.target.value })
                  }
                  className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                  placeholder="Abarrotes, Dulces…"
                  required
                />
              </label>
              <label className="space-y-1">
                <span className="text-xs font-medium text-stone-600">Precio neto</span>
                <input
                  value={formulario.precioNeto}
                  onChange={(e) =>
                    setFormulario({ ...formulario, precioNeto: e.target.value })
                  }
                  className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                  inputMode="decimal"
                  required
                />
              </label>
              <label className="space-y-1">
                <span className="text-xs font-medium text-stone-600">Ganancia</span>
                <input
                  value={formulario.ganancia}
                  onChange={(e) =>
                    setFormulario({ ...formulario, ganancia: e.target.value })
                  }
                  className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                  inputMode="decimal"
                  required
                />
              </label>
              <label className="space-y-1">
                <span className="text-xs font-medium text-stone-600">Existencias</span>
                <input
                  value={formulario.cantidadStock}
                  onChange={(e) =>
                    setFormulario({ ...formulario, cantidadStock: e.target.value })
                  }
                  className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                  inputMode="numeric"
                  required
                />
              </label>
              <label className="space-y-1">
                <span className="text-xs font-medium text-stone-600">Mínimo</span>
                <input
                  value={formulario.stockMinimo}
                  onChange={(e) =>
                    setFormulario({ ...formulario, stockMinimo: e.target.value })
                  }
                  className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
                  inputMode="numeric"
                  required
                />
              </label>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setFormulario(null)}
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
