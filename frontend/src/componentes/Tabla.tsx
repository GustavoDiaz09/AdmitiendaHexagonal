import type { ReactNode } from 'react'

/** Definición de una columna de la tabla: título y cómo renderizar la celda. */
export interface Columna<T> {
  titulo: string
  celda: (fila: T) => ReactNode
}

interface Props<T> {
  filas: T[]
  columnas: Columna<T>[]
  llave: (fila: T) => string | number
  mensajeVacio?: string
}

/** Tabla genérica con el estilo de Admitienda (DRY entre las vistas). */
export function Tabla<T>({ filas, columnas, llave, mensajeVacio }: Props<T>) {
  return (
    <div className="overflow-x-auto rounded-2xl bg-white p-6 shadow-sm">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-stone-200 text-left text-stone-500">
            {columnas.map((columna) => (
              <th key={columna.titulo} className="pb-2 font-medium">
                {columna.titulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {filas.map((fila) => (
            <tr key={llave(fila)} className="border-b border-stone-100">
              {columnas.map((columna) => (
                <td key={columna.titulo} className="py-2">
                  {columna.celda(fila)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
      {filas.length === 0 && mensajeVacio && (
        <p className="py-6 text-center text-stone-400">{mensajeVacio}</p>
      )}
    </div>
  )
}
