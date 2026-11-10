import { useEffect, useState } from 'react'
import { CheckCircle, Trash, XCircle } from '@phosphor-icons/react'
import { useSesion } from '../almacen'
import { api } from '../api'
import { Tabla, type Columna } from '../componentes/Tabla'
import { Aviso, Boton, Cabecera, Insignia, Tarjeta } from '../componentes/ui'
import { fecha } from '../formato'
import type { Rol, SolicitudAdmin, Usuario } from '../tipos'

const ROLES: Rol[] = ['INVITADO', 'REGISTRADO', 'ADMIN']

/** Administración de cuentas: usuarios y solicitudes de permiso. */
export function Usuarios() {
  const yo = useSesion((s) => s.usuario)
  const [usuarios, setUsuarios] = useState<Usuario[]>([])
  const [solicitudes, setSolicitudes] = useState<SolicitudAdmin[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')

  const esAdmin = yo?.rol === 'ADMIN'

  function cargar() {
    if (!esAdmin) {
      setCargando(false)
      return
    }
    setCargando(true)
    api
      .listarUsuarios()
      .then(setUsuarios)
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false))
    api
      .listarSolicitudes()
      .then(setSolicitudes)
      .catch(() => setSolicitudes([]))
  }

  useEffect(cargar, [esAdmin])

  async function cambiarRol(usuario: Usuario, rol: Rol) {
    setError('')
    setAviso('')
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
    setAviso('')
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
    setAviso('')
    try {
      await api.solicitarPermiso('Solicitud enviada desde la vista de usuarios.')
      setAviso('Tu solicitud quedó pendiente para un administrador.')
      cargar()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo enviar.')
    }
  }

  const pendientes = solicitudes.filter((s) => s.estado === 'PENDIENTE')

  const columnas: Columna<Usuario>[] = [
    {
      titulo: 'Usuario',
      celda: (u) => (
        <span className="font-medium text-stone-800">
          {u.nombreUsuario}
          {u.id === yo?.id && <span className="ml-2 text-xs font-normal text-stone-400">(tú)</span>}
        </span>
      ),
    },
    {
      titulo: 'Rol',
      celda: (u) => (
        <select
          value={u.rol}
          disabled={u.id === yo?.id}
          aria-label={`Rol de ${u.nombreUsuario}`}
          title={u.id === yo?.id ? 'No puedes cambiar tu propio rol' : undefined}
          onChange={(e) => cambiarRol(u, e.target.value as Rol)}
          className="rounded-lg border border-stone-300 bg-white px-2.5 py-1.5 text-xs text-stone-800 focus:border-emerald-700 focus:outline-none disabled:opacity-50"
        >
          {ROLES.map((rol) => (
            <option key={rol} value={rol}>
              {rol}
            </option>
          ))}
        </select>
      ),
    },
    {
      titulo: 'Registrado',
      celda: (u) => <span className="text-stone-500">{fecha(u.fechaRegistro)}</span>,
    },
    {
      titulo: '',
      derecha: true,
      celda: (u) => (
        <button
          type="button"
          onClick={() => eliminar(u)}
          disabled={u.id === yo?.id}
          aria-label={`Eliminar ${u.nombreUsuario}`}
          title={u.id === yo?.id ? 'No puedes eliminar tu propia cuenta' : undefined}
          className="rounded-md p-2 text-stone-500 hover:bg-red-50 hover:text-red-700 disabled:opacity-40"
        >
          <Trash size={16} />
        </button>
      ),
    },
  ]

  return (
    <>
      <Cabecera titulo="Usuarios" subtitulo="Cuentas de la tienda y permisos de administrador." />

      {error && <Aviso tono="error">{error}</Aviso>}
      {aviso && <Aviso tono="exito">{aviso}</Aviso>}

      {yo?.rol === 'REGISTRADO' && (
        <Tarjeta className="flex flex-wrap items-center justify-between gap-4">
          <p className="max-w-xl text-sm text-stone-600">
            Como usuario registrado puedes pedir permiso de administrador para gestionar inventario, caja y
            fiado.
          </p>
          <Boton variante="secundario" onClick={solicitarPermiso}>
            Solicitar permiso
          </Boton>
        </Tarjeta>
      )}

      {esAdmin && pendientes.length > 0 && (
        <section className="space-y-3">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-stone-500">
            Solicitudes pendientes ({pendientes.length})
          </h2>
          <ul className="grid gap-3 md:grid-cols-2">
            {pendientes.map((s) => (
              <li key={s.id}>
                <Tarjeta className="flex h-full flex-col gap-4">
                  <div>
                    <p className="font-medium text-stone-900">{s.nombreUsuario}</p>
                    <p className="mt-1 text-sm text-stone-500">
                      {s.motivo || 'Sin motivo'} · {fecha(s.fechaSolicitud)}
                    </p>
                  </div>
                  <div className="mt-auto flex gap-2">
                    <Boton onClick={() => decidir(s.id, true)} className="px-3 py-1.5 text-xs">
                      <CheckCircle size={14} /> Aprobar
                    </Boton>
                    <Boton variante="secundario" onClick={() => decidir(s.id, false)} className="px-3 py-1.5 text-xs">
                      <XCircle size={14} /> Rechazar
                    </Boton>
                  </div>
                </Tarjeta>
              </li>
            ))}
          </ul>
        </section>
      )}

      {esAdmin && (
        <Tabla filas={usuarios} llave={(u) => u.id} columnas={columnas} cargando={cargando} />
      )}

      {yo?.rol === 'REGISTRADO' && (
        <p className="text-sm text-stone-500">
          <Insignia tono="gris">Acceso limitado</Insignia> La lista de cuentas es solo para administradores.
        </p>
      )}
    </>
  )
}
