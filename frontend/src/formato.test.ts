import { describe, expect, it } from 'vitest'
import { aNumero, fecha, pesos, pesosExactos } from './formato'

describe('pesos', () => {
  it('formatea montos enteros sin centavos', () => {
    expect(pesos(150000)).toContain('150.000')
  })

  it('formatea montos con centavos cuando hacen falta', () => {
    expect(pesosExactos(12500.5)).toContain('12.500,50')
  })

  it('oculta los centavos cuando son cero', () => {
    expect(pesosExactos(2000)).not.toContain(',00')
  })
})

describe('aNumero', () => {
  it('acepta coma decimal', () => {
    expect(aNumero('12500,5')).toBe(12500.5)
  })

  it('acepta punto decimal', () => {
    expect(aNumero('12500.5')).toBe(12500.5)
  })
})

describe('fecha', () => {
  it('formatea en hora Colombia', () => {
    // 2026-11-09T00:00:00Z son las 19:00 del 8 de noviembre en Bogotá:
    // el día debe ser 8, no 9, y la hora 7 p. m.
    const texto = fecha(new Date('2026-11-09T00:00:00Z').getTime())
    expect(texto).toContain('8/11/2026')
    expect(texto).toContain('7:00')
  })
})
