import { useEffect, useId, type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode } from 'react'
import { X } from '@phosphor-icons/react'

/** Encabezado de cada pantalla: título, subtítulo y acciones a la derecha. */
export function Cabecera({
  titulo,
  subtitulo,
  acciones,
}: {
  titulo: string
  subtitulo?: string
  acciones?: ReactNode
}) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-stone-900">{titulo}</h1>
        {subtitulo && <p className="mt-1 text-sm text-stone-500">{subtitulo}</p>}
      </div>
      {acciones}
    </header>
  )
}

/** Superficie blanca con borde suave; base de casi todos los bloques. */
export function Tarjeta({ children, className = '' }: { children: ReactNode; className?: string }) {
  return (
    <section className={`rounded-2xl border border-stone-200/80 bg-white p-6 ${className}`}>
      {children}
    </section>
  )
}

type Tono = 'neutro' | 'positivo' | 'negativo' | 'alerta'

const COLOR_TONO: Record<Tono, string> = {
  neutro: 'text-stone-900',
  positivo: 'text-emerald-800',
  negativo: 'text-red-700',
  alerta: 'text-amber-700',
}

/** Cifra destacada con etiqueta, para saldos y totales. */
export function Estadistica({
  etiqueta,
  valor,
  tono = 'neutro',
}: {
  etiqueta: string
  valor: string
  tono?: Tono
}) {
  return (
    <Tarjeta>
      <p className="text-xs font-medium uppercase tracking-wider text-stone-500">{etiqueta}</p>
      <p className={`mt-2 text-3xl font-semibold tabular-nums ${COLOR_TONO[tono]}`}>{valor}</p>
    </Tarjeta>
  )
}

type Variante = 'primario' | 'secundario' | 'peligro' | 'fantasma'

const CLASES_VARIANTE: Record<Variante, string> = {
  primario: 'bg-emerald-600 text-white hover:bg-emerald-700',
  secundario: 'bg-stone-100 text-stone-800 hover:bg-stone-200',
  peligro: 'bg-red-700 text-white hover:bg-red-800',
  fantasma: 'text-stone-600 hover:bg-stone-100',
}

/** Botón con variantes; por defecto es type="button" para no enviar formularios por accidente. */
export function Boton({
  variante = 'primario',
  className = '',
  type = 'button',
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variante?: Variante }) {
  return (
    <button
      {...props}
      type={type}
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-700 disabled:cursor-not-allowed disabled:opacity-50 ${CLASES_VARIANTE[variante]} ${className}`}
    />
  )
}

/** Campo de formulario con etiqueta asociada, para que los lectores de pantalla lo anuncien. */
export function Campo({
  etiqueta,
  ayuda,
  ...entrada
}: InputHTMLAttributes<HTMLInputElement> & { etiqueta: string; ayuda?: string }) {
  const id = useId()
  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm font-medium text-stone-700">
        {etiqueta}
      </label>
      <input
        id={id}
        {...entrada}
        className="w-full rounded-lg border border-stone-300 bg-white px-3 py-2 text-sm text-stone-900 placeholder:text-stone-400 focus:border-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
      />
      {ayuda && <p className="text-xs text-stone-500">{ayuda}</p>}
    </div>
  )
}

/** Diálogo modal accesible: se cierra con Escape, con clic fuera o con su botón. */
export function Modal({
  titulo,
  onCerrar,
  children,
}: {
  titulo: string
  onCerrar: () => void
  children: ReactNode
}) {
  const idTitulo = useId()

  useEffect(() => {
    function alPresionar(e: KeyboardEvent) {
      if (e.key === 'Escape') onCerrar()
    }
    window.addEventListener('keydown', alPresionar)
    return () => window.removeEventListener('keydown', alPresionar)
  }, [onCerrar])

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-stone-900/40 p-4"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onCerrar()
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl"
      >
        <div className="mb-5 flex items-center justify-between">
          <h2 id={idTitulo} className="text-lg font-semibold text-stone-900">
            {titulo}
          </h2>
          <button
            type="button"
            onClick={onCerrar}
            aria-label="Cerrar"
            className="rounded-md p-1 text-stone-500 hover:bg-stone-100"
          >
            <X size={18} />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

/** Mensaje de estado: error o confirmación. */
export function Aviso({ tono, children }: { tono: 'error' | 'exito'; children: ReactNode }) {
  const clases =
    tono === 'error' ? 'bg-red-50 text-red-800 border-red-200' : 'bg-emerald-50 text-emerald-800 border-emerald-200'
  return (
    <p role={tono === 'error' ? 'alert' : 'status'} className={`rounded-xl border px-4 py-3 text-sm ${clases}`}>
      {children}
    </p>
  )
}

const CLASES_INSIGNIA = {
  verde: 'bg-emerald-50 text-emerald-800 ring-emerald-600/20',
  ambar: 'bg-amber-50 text-amber-800 ring-amber-600/20',
  rojo: 'bg-red-50 text-red-700 ring-red-600/20',
  gris: 'bg-stone-100 text-stone-600 ring-stone-500/20',
}

/** Etiqueta pequeña para estados (stock bajo, saldada, ingreso, egreso). */
export function Insignia({
  tono,
  children,
}: {
  tono: keyof typeof CLASES_INSIGNIA
  children: ReactNode
}) {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${CLASES_INSIGNIA[tono]}`}
    >
      {children}
    </span>
  )
}
