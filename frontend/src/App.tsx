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
        <Route index element={<Navigate to="/resumen" replace />} />
        <Route path="/resumen" element={<Resumen />} />
        <Route path="/productos" element={<Productos />} />
        <Route path="/movimientos" element={<Movimientos />} />
        <Route path="/deudas" element={<Deudas />} />
        <Route path="/alertas" element={<Alertas />} />
        <Route path="/usuarios" element={<Usuarios />} />
        <Route path="*" element={<Navigate to="/resumen" replace />} />
      </Route>
    </Routes>
  )
}
