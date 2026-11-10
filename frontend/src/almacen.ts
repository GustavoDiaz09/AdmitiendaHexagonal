/** Almacén de la sesión: usuario autenticado y acciones de entrada/salida. */

import { create } from 'zustand'
import { api, ErrorDeApi, olvidarToken, tokenActual } from './api'
import type { Rol } from './tipos'

interface EstadoDeSesion {
  usuario: { id: number; nombreUsuario: string; rol: Rol } | null
  cargando: boolean
  restaurar: () => Promise<void>
  entrar: (nombreUsuario: string, contrasena: string) => Promise<void>
  salir: () => Promise<void>
}

export const useSesion = create<EstadoDeSesion>((set) => ({
  usuario: null,
  cargando: true,

  restaurar: async () => {
    if (!tokenActual()) {
      set({ cargando: false })
      return
    }
    try {
      const { usuario } = await api.sesionActual()
      set({ usuario, cargando: false })
    } catch (e) {
      if (e instanceof ErrorDeApi && e.estado === 401) {
        olvidarToken()
      }
      set({ usuario: null, cargando: false })
    }
  },

  entrar: async (nombreUsuario, contrasena) => {
    const sesion = await api.iniciarSesion(nombreUsuario, contrasena)
    api.guardarToken(sesion.token)
    set({ usuario: sesion.usuario })
  },

  salir: async () => {
    try {
      await api.cerrarSesion()
    } catch {
      // la sesión puede ya estar vencida: se olvida igual
    }
    olvidarToken()
    set({ usuario: null })
  },
}))
