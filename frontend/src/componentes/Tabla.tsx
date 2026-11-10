import type { ReactNode } from 'react'

/** Definición de una columna de la tabla: título y cómo renderizar la celda. */
export interface Columna<T> {
  titulo: string
  celda: (fila: T) => ReactNode
  /** Alinea la columna a la derecha (montos y acciones). */
  derecha?: boolean
}

interface Props<T> {
  filas: T[]
  columnas: Columna<T>[]
  llave: (fila: T) => string | number
  mensajeVacio?: string
  cargando?: boolean
  /** Filas más bajas y texto más pequeño, para resúmenes densos. */
  compacta?: boolean
}

/** Tabla genérica con el estilo de Admitienda (DRY entre las vistas). */
export function Tabla<T>({ filas, columnas, llave, mensajeVacio, cargando = false, compacta = false }: Props<T>) {
  const celda = compacta ? 'px-4 py-2' : 'px-5 py-3.5'
  const cabecera = compacta ? 'px-4 py-2' : 'px-5 py-3'
  return (
    <div className="overflow-hidden rounded-xl border border-stone-200/80 bg-white">
      <div className="overflow-x-auto">
        <table className={`w-full text-sm ${compacta ? '' : 'min-w-[32rem]'}`}>
          <thead className="bg-stone-50/80">
            <tr className="border-b border-stone-200">
              {columnas.map((columna, indice) => (
                <th
                  key={indice}
                  scope="col"
                  className={`${cabecera} text-[11px] font-semibold uppercase tracking-wider text-stone-500 ${
                    columna.derecha ? 'text-right' : 'text-left'
                  }`}
                >
                  {columna.titulo}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-stone-100">
            {filas.map((fila) => (
              <tr key={llave(fila)} className="transition hover:bg-stone-50/60">
                {columnas.map((columna, indice) => (
                  <td
                    key={indice}
                    className={`${celda} align-middle ${columna.derecha ? 'text-right' : ''}`}
                  >
                    {columna.celda(fila)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {cargando && (
        <p className="px-5 py-10 text-center text-sm text-stone-400">Cargando…</p>
      )}
      {!cargando && filas.length === 0 && mensajeVacio && (
        <p className="px-5 py-10 text-center text-sm text-stone-400">{mensajeVacio}</p>
      )}
    </div>
  )
}
