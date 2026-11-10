import { useEffect } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { useSesion } from './almacen'
import { Shell } from './vistas/Shell'
import { Login } from './vistas/Login'
import { Registro } from './vistas/Registro'
import { Productos } from './vistas/Productos'
import { Movimientos } from './vistas/Movimientos'
import { Deudas } from './vistas/Deudas'
import { Resumen } from './vistas/Resumen'
import { Alertas } from './vistas/Alertas'
import { Usuarios } from './vistas/Usuarios'
import { inicioDe, puedeVer } from './permisos'
import type { ReactNode } from 'react'

/** Muestra la vista solo si el rol del usuario tiene acceso; si no, lo lleva a su inicio. */
function Permitida({ ruta, children }: { ruta: string; children: ReactNode }) {
  const usuario = useSesion((s) => s.usuario)
  if (!usuario) {
    return null
  }
  return puedeVer(usuario.rol, ruta) ? children : <Navigate to={inicioDe(usuario.rol)} replace />
}

/** Rutas de la aplicación: públicas (login/registro) y protegidas. */
export function App() {
  const { usuario, cargando, restaurar } = useSesion()

  useEffect(() => {
    restaurar()
  }, [restaurar])

  if (cargando) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-stone-100">
        <p className="text-stone-500">Cargando Admitienda…</p>
      </div>
    )
  }

  if (!usuario) {
    return (
      <Routes>
        <Route path="/registro" element={<Registro />} />
        <Route path="/login" element={<Login />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    )
  }

  return (
    <Routes>
      <Route element={<Shell />}>
        <Route index element={<Navigate to={inicioDe(usuario.rol)} replace />} />
        <Route path="/resumen" element={<Permitida ruta="/resumen"><Resumen /></Permitida>} />
        <Route path="/productos" element={<Permitida ruta="/productos"><Productos /></Permitida>} />
        <Route path="/movimientos" element={<Permitida ruta="/movimientos"><Movimientos /></Permitida>} />
        <Route path="/deudas" element={<Permitida ruta="/deudas"><Deudas /></Permitida>} />
        <Route path="/alertas" element={<Permitida ruta="/alertas"><Alertas /></Permitida>} />
        <Route path="/usuarios" element={<Permitida ruta="/usuarios"><Usuarios /></Permitida>} />
        <Route path="*" element={<Navigate to={inicioDe(usuario.rol)} replace />} />
      </Route>
    </Routes>
  )
}
