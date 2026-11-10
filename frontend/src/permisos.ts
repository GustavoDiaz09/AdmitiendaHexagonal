/**
 * Qué rol puede ver cada ruta de la aplicación. Refleja las reglas del
 * backend (ReglasDeAcceso y casos de uso): INVITADO solo consulta el
 * catálogo; REGISTRADO lee la caja, el fiado y las alertas; ADMIN gestiona.
 */

import type { Rol } from './tipos'

export const RUTAS_POR_ROL: Record<string, Rol[]> = {
  '/resumen': ['REGISTRADO', 'ADMIN'],
  '/productos': ['INVITADO', 'REGISTRADO', 'ADMIN'],
  '/movimientos': ['REGISTRADO', 'ADMIN'],
  '/deudas': ['REGISTRADO', 'ADMIN'],
  '/alertas': ['REGISTRADO', 'ADMIN'],
  '/usuarios': ['REGISTRADO', 'ADMIN'],
}

/** Primera pantalla a la que puede entrar cada rol tras iniciar sesión. */
export function inicioDe(rol: Rol): string {
  return puedeVer(rol, '/resumen') ? '/resumen' : '/productos'
}

export function puedeVer(rol: Rol, ruta: string): boolean {
  return RUTAS_POR_ROL[ruta]?.includes(rol) ?? false
}
