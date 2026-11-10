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
    <header className="flex flex-wrap items-end justify-between gap-x-6 gap-y-4">
      <div>
        <h1 className="text-3xl font-semibold text-stone-900">{titulo}</h1>
        {subtitulo && <p className="mt-1.5 text-base text-stone-500">{subtitulo}</p>}
      </div>
      {acciones}
    </header>
  )
}

/** Superficie blanca: el contraste con el fondo celeste ya separa el contenido. */
export function Tarjeta({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <section className={`rounded-2xl bg-white p-6 ${className}`}>{children}</section>
}

type Tono = 'neutro' | 'positivo' | 'negativo' | 'alerta'

const COLOR_TONO: Record<Tono, string> = {
  neutro: 'text-stone-900',
  positivo: 'text-emerald-800',
  negativo: 'text-red-700',
  alerta: 'text-amber-700',
}

/** Cifra destacada: número grande, etiqueta discreta y sin caja alrededor. */
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
    <div className="space-y-1">
      <p className="text-sm font-medium text-stone-500">{etiqueta}</p>
      <p className={`text-4xl font-semibold tabular-nums tracking-tight ${COLOR_TONO[tono]}`}>{valor}</p>
    </div>
  )
}

type Variante = 'primario' | 'secundario' | 'peligro' | 'fantasma'

const CLASES_VARIANTE: Record<Variante, string> = {
  primario: 'bg-emerald-700 text-white hover:bg-emerald-800',
  secundario: 'bg-emerald-100/70 text-emerald-800 hover:bg-emerald-100',
  peligro: 'bg-red-700 text-white hover:bg-red-800',
  fantasma: 'text-stone-600 hover:bg-stone-100 hover:text-stone-900',
}

/** Botón con retroalimentación al presionar; type="button" por defecto para no enviar formularios por accidente. */
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
      className={`inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition duration-200 ease-out active:scale-[0.98] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-600 disabled:cursor-not-allowed disabled:opacity-50 disabled:active:scale-100 ${CLASES_VARIANTE[variante]} ${className}`}
    />
  )
}

/** Campo con etiqueta asociada; el foco se marca con un anillo del acento. */
export function Campo({
  etiqueta,
  ayuda,
  ...entrada
}: InputHTMLAttributes<HTMLInputElement> & { etiqueta: string; ayuda?: string }) {
  const id = useId()
  return (
    <div className="space-y-2">
      <label htmlFor={id} className="block text-sm font-medium text-stone-700">
        {etiqueta}
      </label>
      <input
        id={id}
        {...entrada}
        className="w-full rounded-xl border border-stone-200 bg-stone-50/60 px-3.5 py-2.5 text-base text-stone-900 transition duration-200 placeholder:text-stone-400 hover:border-stone-300 focus:border-emerald-600 focus:bg-white focus:outline-none focus:ring-4 focus:ring-emerald-600/15"
      />
      {ayuda && <p className="text-sm text-stone-500">{ayuda}</p>}
    </div>
  )
}

/** Diálogo modal: se cierra con Escape, con clic fuera o con su botón. */
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
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/30 p-4 backdrop-blur-[2px]"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onCerrar()
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        className="w-full max-w-md rounded-3xl bg-white p-7 shadow-2xl shadow-slate-900/10"
      >
        <div className="mb-6 flex items-center justify-between gap-4">
          <h2 id={idTitulo} className="text-xl font-semibold text-stone-900">
            {titulo}
          </h2>
          <button
            type="button"
            onClick={onCerrar}
            aria-label="Cerrar"
            className="rounded-lg p-1.5 text-stone-500 transition-colors hover:bg-stone-100 hover:text-stone-900"
          >
            <X size={18} />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

/** Mensaje de estado: error o confirmación, con color suave y texto directo. */
export function Aviso({ tono, children }: { tono: 'error' | 'exito'; children: ReactNode }) {
  const clases = tono === 'error' ? 'bg-red-50 text-red-800' : 'bg-emerald-50 text-emerald-800'
  return (
    <p role={tono === 'error' ? 'alert' : 'status'} className={`rounded-xl px-4 py-3 text-sm font-medium ${clases}`}>
      {children}
    </p>
  )
}

const CLASES_INSIGNIA = {
  verde: 'bg-emerald-100 text-emerald-800',
  ambar: 'bg-amber-100 text-amber-800',
  rojo: 'bg-red-100 text-red-700',
  gris: 'bg-stone-100 text-stone-600',
}

/** Etiqueta corta para estados: stock bajo, saldada, ingreso o egreso. */
export function Insignia({
  tono,
  children,
}: {
  tono: keyof typeof CLASES_INSIGNIA
  children: ReactNode
}) {
  return (
    <span className={`inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-xs font-semibold ${CLASES_INSIGNIA[tono]}`}>
      {children}
    </span>
  )
}
