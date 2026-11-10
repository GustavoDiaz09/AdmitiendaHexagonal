import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Storefront } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { ErrorDeApi } from '../api'
import { Aviso, Boton, Campo } from '../componentes/ui'

/** Pantalla de inicio de sesión. */
export function Login() {
  const entrar = useSesion((s) => s.entrar)
  const navegar = useNavigate()
  const [nombreUsuario, setNombreUsuario] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setEnviando(true)
    try {
      await entrar(nombreUsuario, contrasena)
      navegar('/resumen')
    } catch (err) {
      // Solo un 401 significa credenciales malas; otros fallos son de conexión o del servidor.
      setError(
        err instanceof ErrorDeApi && err.estado === 401
          ? 'Usuario o contraseña incorrectos.'
          : 'No pudimos conectar con el servidor. Intenta de nuevo.',
      )
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
          <h1 className="mt-4 text-2xl font-semibold tracking-tight text-stone-900">Admitienda</h1>
          <p className="mt-1 text-sm text-stone-500">Caja, inventario y fiado de tu tienda.</p>
        </div>

        <form
          onSubmit={enviar}
          className="space-y-5 rounded-2xl border border-stone-200/80 bg-white p-7"
        >
          <Campo
            etiqueta="Usuario"
            value={nombreUsuario}
            onChange={(e) => setNombreUsuario(e.target.value)}
            autoComplete="username"
            required
          />
          <Campo
            etiqueta="Contraseña"
            type="password"
            value={contrasena}
            onChange={(e) => setContrasena(e.target.value)}
            autoComplete="current-password"
            required
          />

          {error && <Aviso tono="error">{error}</Aviso>}

          <Boton type="submit" disabled={enviando} className="w-full py-2.5">
            {enviando ? 'Entrando…' : 'Entrar'}
          </Boton>
        </form>

        <p className="mt-6 text-center text-sm text-stone-500">
          ¿Primera vez?{' '}
          <Link to="/registro" className="font-medium text-emerald-800 hover:underline">
            Crea tu cuenta
          </Link>
        </p>
      </div>
    </div>
  )
}
