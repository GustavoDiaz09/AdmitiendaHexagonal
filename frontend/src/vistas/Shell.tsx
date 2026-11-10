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

/** Marco de la aplicación autenticada: barra lateral (en móvil, superior) y contenido. */
export function Shell() {
  const { usuario, salir } = useSesion()
  const navegar = useNavigate()

  async function cerrar() {
    await salir()
    navegar('/login')
  }

  return (
    <div className="min-h-screen md:flex">
      <aside className="sticky top-0 z-40 bg-emerald-950 px-4 py-3 text-emerald-50 md:flex md:h-screen md:w-64 md:flex-col md:px-5 md:py-7">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <span className="flex size-9 items-center justify-center rounded-xl bg-white/10">
              <Storefront size={20} weight="fill" />
            </span>
            <span className="text-base font-semibold tracking-tight text-white">Admitienda</span>
          </div>
          <button
            type="button"
            onClick={cerrar}
            aria-label="Cerrar sesión"
            className="rounded-lg p-2 text-emerald-100/70 hover:bg-white/10 hover:text-white md:hidden"
          >
            <SignOut size={18} />
          </button>
        </div>

        <nav aria-label="Principal" className="mt-4 flex gap-1 overflow-x-auto md:mt-10 md:flex-col">
          {NAVES.filter(({ ruta }) => usuario && puedeVer(usuario.rol, ruta)).map(
            ({ ruta, nombre, icono: Icono }) => (
              <NavLink
                key={ruta}
                to={ruta}
                className={({ isActive }) =>
                  `flex shrink-0 items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition focus-visible:outline-2 focus-visible:outline-emerald-300 ${
                    isActive
                      ? 'bg-white/10 text-white'
                      : 'text-emerald-100/70 hover:bg-white/5 hover:text-white'
                  }`
                }
              >
                <Icono size={18} />
                {nombre}
              </NavLink>
            ),
          )}
        </nav>

        <div className="mt-auto hidden space-y-4 border-t border-white/10 pt-5 md:block">
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-white" title={usuario?.nombreUsuario}>
              {usuario?.nombreUsuario}
            </p>
            <p className="text-xs text-emerald-200/70">{NOMBRE_ROL[usuario?.rol ?? ''] ?? ''}</p>
          </div>
          <button
            type="button"
            onClick={cerrar}
            className="flex items-center gap-2 text-sm font-medium text-emerald-100/80 hover:text-white"
          >
            <SignOut size={16} /> Cerrar sesión
          </button>
        </div>
      </aside>

      <main className="min-w-0 flex-1 px-4 py-6 md:px-10 md:py-10">
        <div className="mx-auto max-w-6xl space-y-8">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
