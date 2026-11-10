import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { SignOut, Storefront } from '@phosphor-icons/react'
import { useSesion } from '../almacen'

/** Pantalla de inicio de sesión. */
export function Login() {
  const entrar = useSesion((s) => s.entrar)
  const navegar = useNavigate()
  const [nombreUsuario, setNombreUsuario] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [error, setError] = useState('')

  async function enviar(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await entrar(nombreUsuario, contrasena)
      navegar('/resumen')
    } catch {
      setError('Usuario o contraseña incorrectos.')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-stone-100 p-4">
      <form
        onSubmit={enviar}
        className="w-full max-w-sm space-y-5 rounded-2xl bg-white p-8 shadow-sm"
      >
        <div className="flex items-center gap-2">
          <Storefront size={32} className="text-emerald-700" weight="fill" />
          <h1 className="text-2xl font-bold text-stone-800">Admitienda</h1>
        </div>
        <p className="text-sm text-stone-500">
          La caja, el inventario y el fiado de tu tienda, en un solo lugar.
        </p>

        <label className="block space-y-1">
          <span className="text-sm font-medium text-stone-600">Usuario</span>
          <input
            value={nombreUsuario}
            onChange={(e) => setNombreUsuario(e.target.value)}
            className="w-full rounded-lg border border-stone-300 px-3 py-2 focus:border-emerald-600 focus:outline-none"
            autoComplete="username"
            required
          />
        </label>

        <label className="block space-y-1">
          <span className="text-sm font-medium text-stone-600">Contraseña</span>
          <input
            type="password"
            value={contrasena}
            onChange={(e) => setContrasena(e.target.value)}
            className="w-full rounded-lg border border-stone-300 px-3 py-2 focus:border-emerald-600 focus:outline-none"
            autoComplete="current-password"
            required
          />
        </label>

        {error && (
          <p className="flex items-center gap-2 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            <SignOut size={16} /> {error}
          </p>
        )}

        <button
          type="submit"
          className="w-full rounded-lg bg-emerald-700 py-2.5 font-semibold text-white transition hover:bg-emerald-800"
        >
          Entrar
        </button>

        <p className="text-center text-sm text-stone-500">
          ¿Primera vez?{' '}
          <Link to="/registro" className="font-medium text-emerald-700 hover:underline">
            Crea tu cuenta
          </Link>
        </p>
      </form>
    </div>
  )
}
