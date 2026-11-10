import { useEffect, useState } from 'react'
import { CheckCircle, Trash, XCircle } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { api } from '../api'
import { fecha } from '../formato'
import type { Rol, SolicitudAdmin, Usuario } from '../tipos'

const ROLES: Rol[] = ['INVITADO', 'REGISTRADO', 'ADMIN']

/** Administración de cuentas: usuarios y solicitudes de permiso. */
export function Usuarios() {
  const yo = useSesion((s) => s.usuario)
  const [usuarios, setUsuarios] = useState<Usuario[]>([])
  const [solicitudes, setSolicitudes] = useState<SolicitudAdmin[]>([])
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')

  function cargar() {
    api
      .listarUsuarios()
      .then(setUsuarios)
      .catch((e) => setError(e.message))
    api
      .listarSolicitudes()
      .then(setSolicitudes)
      .catch(() => setSolicitudes([]))
  }

  useEffect(cargar, [])

  async function cambiarRol(usuario: Usuario, rol: Rol) {
    setError('')
    try {
      await api.cambiarRol(usuario.id, rol)
      cargar()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo cambiar el rol.')
    }
  }

  async function eliminar(usuario: Usuario) {
    if (!confirm(`¿Eliminar la cuenta "${usuario.nombreUsuario}"?`)) {
      return
    }
    setError('')
    try {
      await api.eliminarUsuario(usuario.id)
      cargar()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo eliminar.')
    }
  }

  async function decidir(id: number, aprobar: boolean) {
    setError('')
    setAviso('')
    try {
      if (aprobar) {
        await api.aprobarSolicitud(id)
        setAviso('Solicitud aprobada: el usuario ahora es administrador.')
      } else {
        await api.rechazarSolicitud(id)
        setAviso('Solicitud rechazada.')
      }
      cargar()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo decidir.')
    }
  }

  async function solicitarPermiso() {
    setError('')
    try {
      await api.solicitarPermiso('Solicitud enviada desde la vista de usuarios.')
      setAviso('Tu solicitud quedó pendiente para un administrador.')
      cargar()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo enviar.')
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-stone-800">Usuarios</h1>

      {error && (
        <p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>
      )}
      {aviso && (
        <p className="rounded-lg bg-emerald-50 px-4 py-3 text-emerald-700">{aviso}</p>
      )}

      {yo?.rol !== 'ADMIN' && (
        <div className="rounded-2xl bg-white p-6 text-sm shadow-sm">
          <p className="text-stone-600">
            Como usuario registrado puedes pedir el permiso de administrador para
            administrar inventario, caja y fiado.
          </p>
          <button
            onClick={solicitarPermiso}
            className="mt-3 rounded-lg bg-amber-600 px-4 py-2 text-sm font-semibold text-white hover:bg-amber-700"
          >
            Solicitar permiso de administrador
          </button>
        </div>
      )}

      {yo?.rol === 'ADMIN' && solicitudes.some((s) => s.estado === 'PENDIENTE') && (
        <section className="rounded-2xl bg-white p-6 shadow-sm">
          <h2 className="mb-4 font-semibold text-stone-700">Solicitudes pendientes</h2>
          <ul className="space-y-3">
            {solicitudes
              .filter((s) => s.estado === 'PENDIENTE')
              .map((s) => (
                <li
                  key={s.id}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-stone-200 p-4"
                >
                  <div>
                    <p className="font-medium text-stone-800">{s.nombreUsuario}</p>
                    <p className="text-sm text-stone-500">
                      {s.motivo || 'Sin motivo'} · {fecha(s.fechaSolicitud)}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => decidir(s.id, true)}
                      className="flex items-center gap-1 rounded-lg bg-emerald-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-800"
                    >
                      <CheckCircle size={14} /> Aprobar
                    </button>
                    <button
                      onClick={() => decidir(s.id, false)}
                      className="flex items-center gap-1 rounded-lg bg-stone-200 px-3 py-1.5 text-xs font-semibold text-stone-700 hover:bg-stone-300"
                    >
                      <XCircle size={14} /> Rechazar
                    </button>
                  </div>
                </li>
              ))}
          </ul>
        </section>
      )}

      <div className="overflow-x-auto rounded-2xl bg-white p-6 shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-stone-200 text-left text-stone-500">
              <th className="pb-2 font-medium">Usuario</th>
              <th className="pb-2 font-medium">Rol</th>
              <th className="pb-2 font-medium">Registrado</th>
              {yo?.rol === 'ADMIN' && <th className="pb-2 font-medium" />}
            </tr>
          </thead>
          <tbody>
            {usuarios.map((u) => (
              <tr key={u.id} className="border-b border-stone-100">
                <td className="py-2 text-stone-700">{u.nombreUsuario}</td>
                <td className="py-2">
                  {yo?.rol === 'ADMIN' ? (
                    <select
                      value={u.rol}
                      onChange={(e) => cambiarRol(u, e.target.value as Rol)}
                      className="rounded-md border border-stone-300 px-2 py-1 text-xs"
                    >
                      {ROLES.map((rol) => (
                        <option key={rol} value={rol}>
                          {rol}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <span className="text-stone-600">{u.rol}</span>
                  )}
                </td>
                <td className="py-2 text-stone-500">{fecha(u.fechaRegistro)}</td>
                {yo?.rol === 'ADMIN' && (
                  <td className="py-2 text-right">
                    <button
                      onClick={() => eliminar(u)}
                      className="rounded-md px-2 py-1 text-xs font-medium text-red-700 hover:bg-red-50"
                    >
                      <Trash size={14} className="inline" /> Eliminar
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
