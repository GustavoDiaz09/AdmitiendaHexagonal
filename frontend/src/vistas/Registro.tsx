import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Storefront } from '@phosphor-icons/react'
import { api } from '../api'
import { Aviso, Boton, Campo } from '../componentes/ui'

/** Pantalla de registro de cuenta nueva (rol REGISTRADO). */
export function Registro() {
  const navegar = useNavigate()
  const [nombreUsuario, setNombreUsuario] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [indicio, setIndicio] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setEnviando(true)
    try {
      await api.registrarUsuario(nombreUsuario, contrasena, indicio)
      navegar('/login')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo crear la cuenta.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-12">
      <div className="w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center text-center">
          <span className="flex size-12 items-center justify-center rounded-2xl bg-emerald-600 text-white">
            <Storefront size={26} weight="fill" />
          </span>
          <h1 className="mt-4 text-2xl font-semibold tracking-tight text-stone-900">Crear cuenta</h1>
          <p className="mt-1 text-sm text-stone-500">
            Verás caja, fiado e inventario. Un administrador puede darte permiso total.
          </p>
        </div>

        <form
          onSubmit={enviar}
          className="space-y-5 rounded-2xl border border-stone-200/80 bg-white p-7"
        >
          <Campo
            etiqueta="Usuario"
            value={nombreUsuario}
            onChange={(e) => setNombreUsuario(e.target.value)}
            minLength={3}
            autoComplete="username"
            required
          />
          <Campo
            etiqueta="Contraseña"
            type="password"
            value={contrasena}
            onChange={(e) => setContrasena(e.target.value)}
            minLength={6}
            autoComplete="new-password"
            ayuda="Mínimo 6 caracteres."
            required
          />
          <Campo
            etiqueta="Indicio (opcional)"
            value={indicio}
            onChange={(e) => setIndicio(e.target.value)}
            placeholder="¿Qué te ayuda a recordar la contraseña?"
          />

          {error && <Aviso tono="error">{error}</Aviso>}

          <Boton type="submit" disabled={enviando} className="w-full py-2.5">
            {enviando ? 'Creando…' : 'Crear cuenta'}
          </Boton>
        </form>

        <p className="mt-6 text-center text-sm text-stone-500">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="font-medium text-emerald-800 hover:underline">
            Entra aquí
          </Link>
        </p>
      </div>
    </div>
  )
}
