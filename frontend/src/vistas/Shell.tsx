import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  ArrowsLeftRight,
  Bank,
  ListChecks,
  ShoppingCart,
  SignOut,
  Storefront,
  UsersThree,
  Warning,
} from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { puedeVer } from '../permisos'

const NAVES = [
  { ruta: '/resumen', nombre: 'Resumen', icono: Bank },
  { ruta: '/productos', nombre: 'Productos', icono: ShoppingCart },
  { ruta: '/movimientos', nombre: 'Caja', icono: ArrowsLeftRight },
  { ruta: '/deudas', nombre: 'Fiado', icono: ListChecks },
  { ruta: '/alertas', nombre: 'Alertas', icono: Warning },
  { ruta: '/usuarios', nombre: 'Usuarios', icono: UsersThree },
]

/** Marco de la aplicación autenticada: barra lateral y contenido. */
export function Shell() {
  const { usuario, salir } = useSesion()
  const navegar = useNavigate()

  async function cerrar() {
    await salir()
    navegar('/login')
  }

  return (
    <div className="min-h-screen bg-stone-100">
      <div className="mx-auto flex max-w-6xl">
        <aside className="flex w-56 min-h-screen flex-col gap-1 border-r border-stone-200 bg-white p-4">
          <div className="mb-4 flex items-center gap-2 px-2">
            <Storefront size={28} className="text-emerald-700" />
            <span className="text-lg font-bold text-stone-800">Admitienda</span>
          </div>

          {NAVES.filter(({ ruta }) => usuario && puedeVer(usuario.rol, ruta)).map(({ ruta, nombre, icono: Icono }) => (
            <NavLink
              key={ruta}
              to={ruta}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition ${
                  isActive
                    ? 'bg-emerald-50 text-emerald-800'
                    : 'text-stone-600 hover:bg-stone-50'
                }`
              }
            >
              <Icono size={20} />
              {nombre}
            </NavLink>
          ))}

          <div className="mt-auto space-y-2 px-2">
            <p className="truncate text-xs text-stone-400" title={usuario?.nombreUsuario}>
              {usuario?.nombreUsuario} · {usuario?.rol}
            </p>
            <button
              onClick={cerrar}
              className="flex items-center gap-2 text-sm font-medium text-red-700 hover:underline"
            >
              <SignOut size={18} /> Cerrar sesión
            </button>
          </div>
        </aside>

        <main className="min-w-0 flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
