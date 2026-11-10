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

const NOMBRE_ROL: Record<string, string> = {
  ADMIN: 'Administrador',
  REGISTRADO: 'Registrado',
  INVITADO: 'Invitado',
}

/** Marco de la aplicación: navegación superior fija y contenido centrado. */
export function Shell() {
  const { usuario, salir } = useSesion()
  const navegar = useNavigate()

  async function cerrar() {
    await salir()
    navegar('/login')
  }

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-40 border-b border-stone-200/70 bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center gap-4 px-4 py-3 md:gap-8 md:px-8">
          <div className="flex shrink-0 items-center gap-2.5">
            <span className="flex size-9 items-center justify-center rounded-xl bg-emerald-700 text-white">
              <Storefront size={20} weight="fill" />
            </span>
            <span className="hidden text-base font-semibold text-stone-900 sm:inline">Admitienda</span>
          </div>

          <nav aria-label="Principal" className="flex min-w-0 flex-1 gap-1 overflow-x-auto">
            {NAVES.filter(({ ruta }) => usuario && puedeVer(usuario.rol, ruta)).map(
              ({ ruta, nombre, icono: Icono }) => (
                <NavLink
                  key={ruta}
                  to={ruta}
                  className={({ isActive }) =>
                    `flex shrink-0 items-center gap-2 rounded-xl px-3 py-2 text-sm font-medium transition-colors duration-200 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-600 ${
                      isActive
                        ? 'bg-emerald-100/70 text-emerald-800'
                        : 'text-stone-600 hover:bg-stone-100 hover:text-stone-900'
                    }`
                  }
                >
                  <Icono size={18} />
                  {nombre}
                </NavLink>
              ),
            )}
          </nav>

          <div className="hidden shrink-0 items-center gap-4 md:flex">
            <div className="text-right leading-tight">
              <p className="text-sm font-medium text-stone-900">{usuario?.nombreUsuario}</p>
              <p className="text-xs text-stone-500">{NOMBRE_ROL[usuario?.rol ?? ''] ?? ''}</p>
            </div>
            <button
              type="button"
              onClick={cerrar}
              className="rounded-xl p-2 text-stone-500 transition-colors duration-200 hover:bg-stone-100 hover:text-stone-900"
              aria-label="Cerrar sesión"
              title="Cerrar sesión"
            >
              <SignOut size={20} />
            </button>
          </div>
          <button
            type="button"
            onClick={cerrar}
            className="shrink-0 rounded-xl p-2 text-stone-500 hover:bg-stone-100 md:hidden"
            aria-label="Cerrar sesión"
          >
            <SignOut size={20} />
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8 md:px-8 md:py-10">
        <div className="space-y-8">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
