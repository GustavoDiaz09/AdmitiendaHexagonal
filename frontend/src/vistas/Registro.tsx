import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'

/** Pantalla de registro de cuenta nueva (rol REGISTRADO). */
export function Registro() {
  const navegar = useNavigate()
  const [nombreUsuario, setNombreUsuario] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [indicio, setIndicio] = useState('')
  const [error, setError] = useState('')

  async function enviar(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.registrarUsuario(nombreUsuario, contrasena, indicio)
      navegar('/login')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo crear la cuenta.')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-stone-100 p-4">
      <form
        onSubmit={enviar}
        className="w-full max-w-sm space-y-5 rounded-2xl bg-white p-8 shadow-sm"
      >
        <h1 className="text-2xl font-bold text-stone-800">Crear cuenta</h1>
        <p className="text-sm text-stone-500">
          Podrás ver caja, fiado e inventario; un administrador puede darte permiso
          total.
        </p>

        <label className="block space-y-1">
          <span className="text-sm font-medium text-stone-600">Usuario</span>
          <input
            value={nombreUsuario}
            onChange={(e) => setNombreUsuario(e.target.value)}
            className="w-full rounded-lg border border-stone-300 px-3 py-2 focus:border-emerald-600 focus:outline-none"
            minLength={3}
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
            minLength={6}
            required
          />
        </label>

        <label className="block space-y-1">
          <span className="text-sm font-medium text-stone-600">
            Indicio (opcional)
          </span>
          <input
            value={indicio}
            onChange={(e) => setIndicio(e.target.value)}
            className="w-full rounded-lg border border-stone-300 px-3 py-2 focus:border-emerald-600 focus:outline-none"
            placeholder="¿Qué te ayuda a recordar la contraseña?"
          />
        </label>

        {error && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}

        <button
          type="submit"
          className="w-full rounded-lg bg-emerald-700 py-2.5 font-semibold text-white transition hover:bg-emerald-800"
        >
          Crear cuenta
        </button>

        <p className="text-center text-sm text-stone-500">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="font-medium text-emerald-700 hover:underline">
            Entra aquí
          </Link>
        </p>
      </form>
    </div>
  )
}
