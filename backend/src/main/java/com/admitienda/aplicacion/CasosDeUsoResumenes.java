package com.admitienda.aplicacion;

import com.admitienda.dominio.modelo.Actor;
import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.modelo.Movimiento;
import com.admitienda.dominio.modelo.TipoMovimiento;
import com.admitienda.dominio.puerto.entrada.ConsultaDeResumenes;
import com.admitienda.dominio.puerto.salida.RepositorioDeudas;
import com.admitienda.dominio.puerto.salida.RepositorioMovimientos;
import com.admitienda.dominio.puerto.salida.Reloj;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caso de uso: resumen financiero. Los días y meses se agrupan con la zona
 * horaria de Colombia y el "hoy" viene del puerto {@link Reloj}, de modo
 * que las pruebas fijan el tiempo y obtienen resultados deterministas.
 */
public final class CasosDeUsoResumenes implements ConsultaDeResumenes {

    private static final ZoneId ZONA_DE_COLOMBIA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private final RepositorioMovimientos movimientos;
    private final RepositorioDeudas deudas;
    private final Reloj reloj;

    public CasosDeUsoResumenes(RepositorioMovimientos movimientos,
                               RepositorioDeudas deudas, Reloj reloj) {
        this.movimientos = movimientos;
        this.deudas = deudas;
        this.reloj = reloj;
    }

    @Override
    public ResumenGeneral obtenerResumen(Actor actor) {
        ReglasDeAcceso.exigirRegistrado(actor, "ver el resumen");
        Dinero saldoCaja = movimientos.totalDe(TipoMovimiento.INGRESO)
                .restar(movimientos.totalDe(TipoMovimiento.EGRESO));
        Dinero totalPorCobrar = deudas.totalPendiente();

        LocalDate hoy = Instant.ofEpochMilli(reloj.ahora()).atZone(ZONA_DE_COLOMBIA).toLocalDate();
        LocalDate primerDia = hoy.minusDays(6);
        YearMonth primerMes = YearMonth.from(hoy.minusMonths(11));

        long desde = primerMes.atDay(1).atStartOfDay(ZONA_DE_COLOMBIA).toInstant().toEpochMilli();
        List<Movimiento> historico = movimientos.buscarEntre(desde, null);
        Map<LocalDate, List<Movimiento>> porDia = historico.stream()
                .collect(Collectors.groupingBy(m -> fechaDe(m.fecha())));
        Map<YearMonth, List<Movimiento>> porMes = historico.stream()
                .collect(Collectors.groupingBy(m -> YearMonth.from(fechaDe(m.fecha()))));

        List<ResumenPorDia> dias = primerDia.datesUntil(hoy.plusDays(1))
                .map(dia -> resumirDia(dia, porDia.getOrDefault(dia, List.of())))
                .toList();
        List<ResumenPorMes> meses = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            YearMonth mes = primerMes.plusMonths(i);
            meses.add(resumirMes(mes, porMes.getOrDefault(mes, List.of())));
        }
        return new ResumenGeneral(saldoCaja, totalPorCobrar, dias, meses);
    }

    private ResumenPorDia resumirDia(LocalDate dia, List<Movimiento> delDia) {
        Dinero ingresos = totalDe(TipoMovimiento.INGRESO, delDia);
        Dinero egresos = totalDe(TipoMovimiento.EGRESO, delDia);
        return new ResumenPorDia(dia.format(FORMATO_DIA), ingresos, egresos,
                ingresos.restar(egresos));
    }

    private ResumenPorMes resumirMes(YearMonth mes, List<Movimiento> delMes) {
        Dinero ingresos = totalDe(TipoMovimiento.INGRESO, delMes);
        Dinero egresos = totalDe(TipoMovimiento.EGRESO, delMes);
        return new ResumenPorMes(mes.format(FORMATO_MES), ingresos, egresos,
                ingresos.restar(egresos));
    }

    private LocalDate fechaDe(long milisegundos) {
        return Instant.ofEpochMilli(milisegundos).atZone(ZONA_DE_COLOMBIA).toLocalDate();
    }

    private Dinero totalDe(TipoMovimiento tipo, List<Movimiento> grupo) {
        return grupo.stream()
                .filter(m -> m.tipo() == tipo)
                .map(Movimiento::monto)
                .reduce(Dinero.cero(), Dinero::sumar);
    }
}
