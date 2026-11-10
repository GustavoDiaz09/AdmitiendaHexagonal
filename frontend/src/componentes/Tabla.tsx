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

/** Filas de esqueleto con la forma de la tabla mientras llegan los datos. */
function Esqueleto({ columnas, compacta }: { columnas: number; compacta: boolean }) {
  return (
    <>
      {[0, 1, 2].map((fila) => (
        <tr key={fila} aria-hidden="true">
          {Array.from({ length: columnas }).map((_, indice) => (
            <td key={indice} className={compacta ? 'px-4 py-2.5' : 'px-5 py-4'}>
              <div
                className="h-3.5 animate-pulse rounded-md bg-stone-100"
                style={{ width: `${55 + ((fila * 17 + indice * 23) % 35)}%` }}
              />
            </td>
          ))}
        </tr>
      ))}
    </>
  )
}

/** Tabla genérica de Admitienda: sin caja exterior, filas legibles y estado de carga. */
export function Tabla<T>({ filas, columnas, llave, mensajeVacio, cargando = false, compacta = false }: Props<T>) {
  const celda = compacta ? 'px-4 py-2.5' : 'px-5 py-4'
  const cabecera = compacta ? 'px-4 py-2' : 'px-5 py-3'
  const vacia = !cargando && filas.length === 0

  return (
    <div className="overflow-hidden rounded-2xl bg-white">
      <div className="overflow-x-auto">
        <table className={`w-full text-base ${compacta ? '' : 'min-w-[34rem]'}`}>
          <thead>
            <tr className="border-b border-stone-100">
              {columnas.map((columna, indice) => (
                <th
                  key={indice}
                  scope="col"
                  className={`${cabecera} text-sm font-medium text-stone-500 ${columna.derecha ? 'text-right' : 'text-left'}`}
                >
                  {columna.titulo}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-stone-100">
            {cargando ? (
              <Esqueleto columnas={columnas.length} compacta={compacta} />
            ) : (
              filas.map((fila) => (
                <tr key={llave(fila)} className="transition-colors duration-150 hover:bg-emerald-50/50">
                  {columnas.map((columna, indice) => (
                    <td key={indice} className={`${celda} align-middle ${columna.derecha ? 'text-right' : ''}`}>
                      {columna.celda(fila)}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {vacia && mensajeVacio && (
        <p className="px-5 py-12 text-center text-base text-stone-500">{mensajeVacio}</p>
      )}
    </div>
  )
}
