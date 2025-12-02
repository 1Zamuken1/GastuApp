package GastuApp.Notificaciones.Service;

import GastuApp.Conceptos.DTO.ConceptoDTO;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.Movimientos.DTO.PreferenciasFinancierasDTO;
import GastuApp.Movimientos.Entity.Movimiento;
import GastuApp.Movimientos.Entity.Movimiento.TipoMovimiento;
import GastuApp.Movimientos.Repository.MovimientoRepository;
import GastuApp.Movimientos.Service.PreferenciasUsuarioService;
import GastuApp.Notificaciones.Entity.Notificacion.TipoNotificacion;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para análisis asíncrono de alertas financieras.
 * Procesa movimientos y genera notificaciones basadas en las preferencias del
 * usuario.
 */
@Service
public class AlertAnalysisService {

    private final MovimientoRepository movimientoRepository;
    private final NotificacionService notificacionService;
    private final PreferenciasUsuarioService preferenciasService;
    private final ConceptoService conceptoService;

    public AlertAnalysisService(
            MovimientoRepository movimientoRepository,
            NotificacionService notificacionService,
            PreferenciasUsuarioService preferenciasService,
            ConceptoService conceptoService) {
        this.movimientoRepository = movimientoRepository;
        this.notificacionService = notificacionService;
        this.preferenciasService = preferenciasService;
        this.conceptoService = conceptoService;
    }

    /**
     * Punto de entrada principal para analizar un movimiento recién
     * creado/actualizado.
     * Se ejecuta asíncronamente para no bloquear la transacción principal.
     * 
     * @param usuarioId  ID del usuario
     * @param movimiento Movimiento a analizar
     * @param tipo       Tipo de movimiento (INGRESO o EGRESO)
     */
    @Async
    public void analizarMovimiento(Long usuarioId, Movimiento movimiento, TipoMovimiento tipo) {
        PreferenciasFinancierasDTO prefs = preferenciasService.obtenerPreferencias(usuarioId);

        if (tipo == TipoMovimiento.EGRESO) {
            analizarEgreso(usuarioId, movimiento, prefs);
        } else {
            analizarIngreso(usuarioId, movimiento, prefs);
        }
    }

    /**
     * Analiza un egreso y ejecuta todas las validaciones configuradas.
     */
    private void analizarEgreso(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        // Alertas Basadas en Tendencias
        if (prefs.getAlertGastoIncrementalEnabled()) {
            checkGastoIncremental(usuarioId, egreso, prefs);
        }
        if (prefs.getAlertPatronInusualEnabled()) {
            checkPatronInusualEgresos(usuarioId, egreso, prefs);
        }

        // Alertas Basadas en Conceptos
        if (prefs.getAlertConcentracionGastosEnabled()) {
            checkConcentracionGastos(usuarioId, egreso, prefs);
        }

        // Alertas Basadas en Tiempo
        if (prefs.getAlertVelocidadGastoEnabled()) {
            checkVelocidadGasto(usuarioId, egreso, prefs);
        }
        if (prefs.getAlertEgresosAgrupadosEnabled()) {
            checkEgresosAgrupados(usuarioId, egreso, prefs);
        }

        // Alertas de Ahorro/Balance
        if (prefs.getAlertBalanceCriticoEnabled()) {
            checkBalanceCritico(usuarioId, egreso, prefs);
        }
        if (prefs.getAlertMetaAhorroEnabled()) {
            checkMetaAhorro(usuarioId, egreso, prefs);
        }

        // Alertas de Sangría (Micro-gastos)
        if (prefs.getAlertMicroGastosEnabled()) {
            checkMicroGastos(usuarioId, egreso, prefs);
        }
        if (prefs.getAlertGastosHormigaEnabled()) {
            checkGastosHormiga(usuarioId, egreso, prefs);
        }

        // Alertas Predictivas
        if (prefs.getAlertProyeccionSobregastoEnabled()) {
            checkProyeccionSobregasto(usuarioId, egreso, prefs);
        }
        if (prefs.getAlertComparacionPeriodoEnabled()) {
            checkComparacionPeriodoEgresos(usuarioId, egreso, prefs);
        }
        if (prefs.getAlertDiaMesCriticoEnabled()) {
            checkDiaMesCritico(usuarioId, egreso, prefs);
        }

        // Alertas de Inconsistencias
        if (prefs.getAlertEgresoSinConceptoEnabled()) {
            checkEgresoSinConcepto(usuarioId, egreso, prefs);
        }
    }

    /**
     * Analiza un ingreso y ejecuta las validaciones configuradas.
     */
    private void analizarIngreso(Long usuarioId, Movimiento ingreso, PreferenciasFinancierasDTO prefs) {
        // Alertas Basadas en Tendencias
        if (prefs.getAlertReduccionIngresosEnabled()) {
            checkReduccionIngresos(usuarioId, ingreso, prefs);
        }

        // Alertas Basadas en Tiempo
        if (prefs.getAlertInactividadIngresosEnabled()) {
            checkInactividadIngresos(usuarioId, ingreso, prefs);
        }

        // Alertas de Inconsistencias
        if (prefs.getAlertIngresoInusualEnabled()) {
            checkIngresoInusual(usuarioId, ingreso, prefs);
        }

        // Alertas Basadas en Conceptos (recurrentes)
        if (prefs.getAlertConceptoSinUsoEnabled()) {
            checkConceptoSinUso(usuarioId, ingreso, prefs);
        }
    }

    // ==================== ALERTAS BASADAS EN TENDENCIAS ====================

    /**
     * Alerta cuando los egresos del mes superan el promedio histórico por un
     * porcentaje configurable.
     */
    private void checkGastoIncremental(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime inicioMesActual = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            BigDecimal totalMesActual = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMesActual, now);

            // Calcular promedio de los últimos N meses
            int meses = prefs.getAlertGastoIncrementalMeses();
            BigDecimal sumaHistorica = BigDecimal.ZERO;

            for (int i = 1; i <= meses; i++) {
                LocalDateTime inicioMes = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime finMes = inicioMes.plusMonths(1).minusSeconds(1);
                BigDecimal totalMes = movimientoRepository.calcularTotalEnRango(
                        usuarioId, TipoMovimiento.EGRESO, inicioMes, finMes);
                sumaHistorica = sumaHistorica.add(totalMes);
            }

            if (sumaHistorica.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal promedio = sumaHistorica.divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_UP);
                BigDecimal porcentajeIncremento = totalMesActual.subtract(promedio)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(promedio, 2, RoundingMode.HALF_UP);

                if (porcentajeIncremento
                        .compareTo(BigDecimal.valueOf(prefs.getAlertGastoIncrementalPorcentaje())) >= 0) {
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            egreso.getId(),
                            "Gasto incremental detectado",
                            String.format(
                                    "Tus egresos este mes ($%.2f) superan el promedio de los últimos %d meses ($%.2f) en un %.2f%%. Considera revisar tus gastos.",
                                    totalMesActual, meses, promedio, porcentajeIncremento));
                }
            }
        } catch (Exception e) {
            // Log error but don't fail
            System.err.println("Error en checkGastoIncremental: " + e.getMessage());
        }
    }

    /**
     * Detecta cuando hay un patrón inusual de transacciones (cantidad anormal en un
     * día/semana).
     */
    private void checkPatronInusualEgresos(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
            LocalDateTime finHoy = inicioHoy.plusDays(1).minusSeconds(1);

            Long transaccionesHoy = movimientoRepository.countByUsuarioIdAndTipoAndFechaRegistroBetween(
                    usuarioId, TipoMovimiento.EGRESO, inicioHoy, finHoy);

            // Calcular promedio histórico de transacciones diarias
            LocalDateTime inicioMesActual = inicioHoy.withDayOfMonth(1);
            Long diasDelMes = ChronoUnit.DAYS.between(inicioMesActual, inicioHoy) + 1;
            Long transaccionesMes = movimientoRepository.countByUsuarioIdAndTipoAndFechaRegistroBetween(
                    usuarioId, TipoMovimiento.EGRESO, inicioMesActual, finHoy);

            if (diasDelMes > 0) {
                double promedioDiario = (double) transaccionesMes / diasDelMes;

                // Alertar si hoy hay más del doble del promedio
                if (transaccionesHoy > promedioDiario * 2 && transaccionesHoy >= 8) {
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            egreso.getId(),
                            "Patrón inusual de gastos",
                            String.format(
                                    "Hoy has registrado %d egresos, significativamente más que tu promedio diario (%.1f). Verifica que no haya errores.",
                                    transaccionesHoy, promedioDiario));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkPatronInusualEgresos: " + e.getMessage());
        }
    }

    /**
     * Detecta cuando los ingresos del mes son menores al promedio histórico.
     */
    private void checkReduccionIngresos(Long usuarioId, Movimiento ingreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime inicioMesActual = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            BigDecimal totalMesActual = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.INGRESO, inicioMesActual, now);

            // Calcular promedio de los últimos 3 meses
            BigDecimal sumaHistorica = BigDecimal.ZERO;

            for (int i = 1; i <= 3; i++) {
                LocalDateTime inicioMes = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime finMes = inicioMes.plusMonths(1).minusSeconds(1);
                BigDecimal totalMes = movimientoRepository.calcularTotalEnRango(
                        usuarioId, TipoMovimiento.INGRESO, inicioMes, finMes);
                sumaHistorica = sumaHistorica.add(totalMes);
            }

            if (sumaHistorica.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal promedio = sumaHistorica.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
                BigDecimal porcentajeReduccion = promedio.subtract(totalMesActual)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(promedio, 2, RoundingMode.HALF_UP);

                if (porcentajeReduccion
                        .compareTo(BigDecimal.valueOf(prefs.getAlertReduccionIngresosPorcentaje())) >= 0) {
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            ingreso.getId(),
                            "Reducción de ingresos detectada",
                            String.format(
                                    "Tus ingresos este mes ($%.2f) son %.2f%% menores al promedio de los últimos 3 meses ($%.2f).",
                                    totalMesActual, porcentajeReduccion, promedio));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkReduccionIngresos: " + e.getMessage());
        }
    }

    // ==================== ALERTAS BASADAS EN CONCEPTOS ====================

    /**
     * Alerta cuando un solo concepto representa más del X% del total de egresos.
     */
    private void checkConcentracionGastos(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime inicioMesActual = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

            List<Object[]> estadisticas = movimientoRepository.obtenerEstadisticasPorConceptoEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMesActual, now);

            BigDecimal totalEgresos = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMesActual, now);

            if (totalEgresos.compareTo(BigDecimal.ZERO) > 0) {
                for (Object[] stat : estadisticas) {
                    Long conceptoId = (Long) stat[0];
                    BigDecimal totalConcepto = (BigDecimal) stat[2];

                    BigDecimal porcentaje = totalConcepto
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalEgresos, 2, RoundingMode.HALF_UP);

                    if (porcentaje.compareTo(BigDecimal.valueOf(prefs.getAlertConcentracionGastosPorcentaje())) >= 0) {
                        ConceptoDTO concepto = conceptoService.obtenerPorId(conceptoId);
                        notificacionService.crearNotificacion(
                                usuarioId,
                                TipoNotificacion.MOVIMIENTO,
                                egreso.getId(),
                                "Concentración de gastos en un concepto",
                                String.format(
                                        "El concepto '%s' representa el %.2f%% de tus egresos este mes ($%.2f de $%.2f). Considera diversificar.",
                                        concepto.getNombre(), porcentaje, totalConcepto, totalEgresos));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkConcentracionGastos: " + e.getMessage());
        }
    }

    /**
     * Detecta conceptos recurrentes que no han tenido actividad en X días
     * (inferencia histórica).
     */
    private void checkConceptoSinUso(Long usuarioId, Movimiento movimiento, PreferenciasFinancierasDTO prefs) {
        try {
            // Inferir conceptos recurrentes: aparecen en al menos 3 de los últimos 6 meses
            LocalDateTime now = LocalDateTime.now();
            Map<Long, Integer> aparicionesPorConcepto = new java.util.HashMap<>();

            for (int i = 1; i <= 6; i++) {
                LocalDateTime inicioMes = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime finMes = inicioMes.plusMonths(1).minusSeconds(1);

                List<Object[]> stats = movimientoRepository.obtenerEstadisticasPorConceptoEnRango(
                        usuarioId, TipoMovimiento.INGRESO, inicioMes, finMes);

                for (Object[] stat : stats) {
                    Long conceptoId = (Long) stat[0];
                    aparicionesPorConcepto.merge(conceptoId, 1, Integer::sum);
                }
            }

            // Conceptos que aparecen en 3+ meses son "recurrentes"
            List<Long> conceptosRecurrentes = aparicionesPorConcepto.entrySet().stream()
                    .filter(e -> e.getValue() >= 3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // Verificar cuáles no tienen registros recientes
            int diasLimite = prefs.getAlertConceptoSinUsoDias();
            LocalDateTime fechaLimite = now.minusDays(diasLimite);

            for (Long conceptoId : conceptosRecurrentes) {
                List<Movimiento> recientes = movimientoRepository.findByUsuarioIdAndTipoAndFechaRegistroAfter(
                        usuarioId, TipoMovimiento.INGRESO, fechaLimite).stream()
                        .filter(m -> conceptoId.equals(m.getConceptoId()))
                        .collect(Collectors.toList());

                if (recientes.isEmpty()) {
                    ConceptoDTO concepto = conceptoService.obtenerPorId(conceptoId);
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.SISTEMA,
                            null,
                            "Concepto recurrente sin actividad",
                            String.format(
                                    "No has registrado ingresos para '%s' en los últimos %d días. ¿Olvidaste registrar algún ingreso?",
                                    concepto.getNombre(), diasLimite));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkConceptoSinUso: " + e.getMessage());
        }
    }

    // ==================== ALERTAS BASADAS EN TIEMPO ====================

    private void checkVelocidadGasto(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int diaDelMes = now.getDayOfMonth();
            LocalDateTime inicioMes = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

            BigDecimal gastoActual = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMes, now);
            BigDecimal ingresoActual = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.INGRESO, inicioMes, now);

            if (ingresoActual.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal porcentajeGastado = gastoActual.multiply(BigDecimal.valueOf(100))
                        .divide(ingresoActual, 2, RoundingMode.HALF_UP);

                // Alerta si gastaste 70%+ pero solo van 15 días o menos
                if (porcentajeGastado.compareTo(BigDecimal.valueOf(70)) >= 0 && diaDelMes <= 15) {
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            egreso.getId(),
                            "Velocidad de gasto alta",
                            String.format(
                                    "Has gastado el %.2f%% de tus ingresos ($%.2f de $%.2f) y solo estamos en el día %d del mes. Modera tus gastos.",
                                    porcentajeGastado, gastoActual, ingresoActual, diaDelMes));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkVelocidadGasto: " + e.getMessage());
        }
    }

    private void checkInactividadIngresos(Long usuarioId, Movimiento ingreso, PreferenciasFinancierasDTO prefs) {
        try {
            Optional<Movimiento> ultimoIngreso = movimientoRepository.findUltimoMovimiento(usuarioId,
                    TipoMovimiento.INGRESO);

            if (ultimoIngreso.isPresent()) {
                long diasDesdeUltimoIngreso = ChronoUnit.DAYS.between(
                        ultimoIngreso.get().getFechaRegistro(), LocalDateTime.now());

                if (diasDesdeUltimoIngreso >= prefs.getAlertInactividadDias()) {
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.SISTEMA,
                            null,
                            "Inactividad de ingresos",
                            String.format(
                                    "Han pasado %d días desde tu último ingreso. ¿Olvidaste registrar alg uno?",
                                    diasDesdeUltimoIngreso));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkInactividadIngresos: " + e.getMessage());
        }
    }

    private void checkEgresosAgrupados(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime horaActual = LocalDateTime.now();
            LocalDateTime horaInicio = horaActual.minusHours(prefs.getAlertEgresosAgrupadosHoras());

            Long cantidadEnPeriodo = movimientoRepository.countByUsuarioIdAndTipoAndFechaRegistroBetween(
                    usuarioId, TipoMovimiento.EGRESO, horaInicio, horaActual);

            if (cantidadEnPeriodo >= prefs.getAlertEgresosAgrupadosCantidad()) {
                notificacionService.crearNotificacion(
                        usuarioId,
                        TipoNotificacion.MOVIMIENTO,
                        egreso.getId(),
                        "Múltiples gastos en corto tiempo",
                        String.format(
                                "Has registrado %d egresos en las últimas %d horas. ¿Compras impulsivas? Revisa tus gastos.",
                                cantidadEnPeriodo, prefs.getAlertEgresosAgrupadosHoras()));
            }
        } catch (Exception e) {
            System.err.println("Error en checkEgresosAgrupados: " + e.getMessage());
        }
    }

    // ==================== ALERTAS DE AHORRO/BALANCE ====================

    private void checkBalanceCritico(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            BigDecimal totalIngresos = movimientoRepository.calcularTotalIngresos(usuarioId);
            BigDecimal totalEgresos = movimientoRepository.calcularTotalEgresos(usuarioId);
            BigDecimal balance = totalIngresos.subtract(totalEgresos);

            // Proyectar balance al final del mes
            LocalDateTime now = LocalDateTime.now();
            int diaDelMes = now.getDayOfMonth();
            int diasDelMes = now.toLocalDate().lengthOfMonth();

            if (diaDelMes > 0) {
                LocalDateTime inicioMes = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                BigDecimal egresosMes = movimientoRepository.calcularTotalEnRango(
                        usuarioId, TipoMovimiento.EGRESO, inicioMes, now);

                BigDecimal promedioEgresoDiario = egresosMes.divide(BigDecimal.valueOf(diaDelMes), 2,
                        RoundingMode.HALF_UP);
                BigDecimal egresoProyectado = promedioEgresoDiario.multiply(BigDecimal.valueOf(diasDelMes));
                BigDecimal balanceProyectado = totalIngresos.subtract(egresoProyectado);

                if (balanceProyectado.compareTo(BigDecimal.ZERO) < 0) {
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            egreso.getId(),
                            "Balance crítico proyectado",
                            String.format(
                                    "A tu ritmo actual de gasto ($%.2f/día), terminarás el mes con un saldo negativo de $%.2f. ¡Reduce tus egresos!",
                                    promedioEgresoDiario, balanceProyectado.abs()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkBalanceCritico: " + e.getMessage());
        }
    }

    private void checkMetaAhorro(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            if (prefs.getMetaAhorroMensual().compareTo(BigDecimal.ZERO) <= 0) {
                return; // No hay meta configurada
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime inicioMes = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

            BigDecimal ingresosMes = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.INGRESO, inicioMes, now);
            BigDecimal egresosMes = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMes, now);

            BigDecimal ahorroActual = ingresosMes.subtract(egresosMes);
            BigDecimal metaAhorro = prefs.getMetaAhorroMensual();

            if (ahorroActual.compareTo(metaAhorro) < 0) {
                BigDecimal diferencia = metaAhorro.subtract(ahorroActual);
                notificacionService.crearNotificacion(
                        usuarioId,
                        TipoNotificacion.AHORRO,
                        egreso.getId(),
                        "Meta de ahorro en riesgo",
                        String.format(
                                "Tu ahorro este mes ($%.2f) está por debajo de tu meta ($%.2f). Te faltan $%.2f para alcanzarla.",
                                ahorroActual, metaAhorro, diferencia));
            }
        } catch (Exception e) {
            System.err.println("Error en checkMetaAhorro: " + e.getMessage());
        }
    }

    // ==================== ALERTAS DE "SANGRÍA" ====================

    private void checkMicroGastos(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime inicioMes = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

            Long cantidadMicroGastos = movimientoRepository.countMicroMovimientos(
                    usuarioId,
                    TipoMovimiento.EGRESO,
                    prefs.getAlertMicroGastosMontoMax(),
                    inicioMes,
                    now);

            if (cantidadMicroGastos >= prefs.getAlertMicroGastosCantidad()) {
                // Calcular total de estos micro-gastos
                List<Movimiento> microMovimientos = movimientoRepository
                        .findByUsuarioIdAndTipoAndFechaRegistroBetween(usuarioId, TipoMovimiento.EGRESO, inicioMes, now)
                        .stream()
                        .filter(m -> m.getMonto().compareTo(prefs.getAlertMicroGastosMontoMax()) <= 0)
                        .collect(Collectors.toList());

                BigDecimal totalMicro = microMovimientos.stream()
                        .map(Movimiento::getMonto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                notificacionService.crearNotificacion(
                        usuarioId,
                        TipoNotificacion.MOVIMIENTO,
                        egreso.getId(),
                        "Múltiples micro-gastos detectados",
                        String.format(
                                "Has registrado %d gastos pequeños (menores a $%.2f) que suman $%.2f este mes. La 'muerte por mil cortes'.",
                                cantidadMicroGastos, prefs.getAlertMicroGastosMontoMax(), totalMicro));
            }
        } catch (Exception e) {
            System.err.println("Error en checkMicroGastos: " + e.getMessage());
        }
    }

    private void checkGastosHormiga(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
            LocalDateTime finHoy = inicioHoy.plusDays(1).minusSeconds(1);

            BigDecimal gastoHoy = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioHoy, finHoy);

            if (gastoHoy.compareTo(prefs.getAlertGastosHormigaMontoMax()) >= 0) {
                Long cantidadTransacciones = movimientoRepository.countByUsuarioIdAndTipoAndFechaRegistroBetween(
                        usuarioId, TipoMovimiento.EGRESO, inicioHoy, finHoy);

                if (cantidadTransacciones >= 3) { // Al menos 3 gastos pequeños
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            egreso.getId(),
                            "Gastos hormiga diarios",
                            String.format(
                                    "Hoy has gastado $%.2f en pequeños gastos (%d transacciones). Estos gastos hormiga se acumulan rápidamente.",
                                    gastoHoy, cantidadTransacciones));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkGastosHormiga: " + e.getMessage());
        }
    }

    // ==================== ALERTAS PREDICTIVAS/PROACTIVAS ====================

    private void checkProyeccionSobregasto(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int diaDelMes = now.getDayOfMonth();
            LocalDateTime inicioMes = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

            BigDecimal egresosMes = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMes, now);
            BigDecimal ingresosMes = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.INGRESO, inicioMes, now);

            if (diaDelMes > 0 && ingresosMes.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal promedioEgresoDiario = egresosMes.divide(BigDecimal.valueOf(diaDelMes), 2,
                        RoundingMode.HALF_UP);
                int diasRestantes = now.toLocalDate().lengthOfMonth() - diaDelMes;
                BigDecimal egresoProyectadoTotal = egresosMes.add(
                        promedioEgresoDiario.multiply(BigDecimal.valueOf(diasRestantes)));

                if (egresoProyectadoTotal.compareTo(ingresosMes) > 0) {
                    BigDecimal sobregasto = egresoProyectadoTotal.subtract(ingresosMes);
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            egreso.getId(),
                            "Proyección de sobregasto",
                            String.format(
                                    "Al ritmo actual ($%.2f/día), gastarás $%.2f este mes, superando tus ingresos ($%.2f) por $%.2f.",
                                    promedioEgresoDiario, egresoProyectadoTotal, ingresosMes, sobregasto));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkProyeccionSobregasto: " + e.getMessage());
        }
    }

    private void checkComparacionPeriodoEgresos(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime inicioMesActual = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            BigDecimal egresosMesActual = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMesActual, now);

            LocalDateTime inicioMesAnterior = now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0)
                    .withSecond(0);
            LocalDateTime finMesAnterior = inicioMesAnterior.plusMonths(1).minusSeconds(1);
            BigDecimal egresosMesAnterior = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMesAnterior, finMesAnterior);

            if (egresosMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diferencia = egresosMesActual.subtract(egresosMesAnterior);
                BigDecimal porcentajeCambio = diferencia.multiply(BigDecimal.valueOf(100))
                        .divide(egresosMesAnterior, 2, RoundingMode.HALF_UP);

                // Alertar si cambió más del 20% (aumento o reducción significativa)
                if (porcentajeCambio.abs().compareTo(BigDecimal.valueOf(20)) >= 0) {
                    String mensaje = porcentajeCambio.compareTo(BigDecimal.ZERO) > 0
                            ? String.format(
                                    "Has gastado %.2f%% MÁS este mes ($%.2f) comparado con el mes pasado ($%.2f).",
                                    porcentajeCambio, egresosMesActual, egresosMesAnterior)
                            : String.format(
                                    "¡Bien! Has gastado %.2f%% MENOS este mes ($%.2f) comparado con el mes pasado ($%.2f). ¡Sigue así!",
                                    porcentajeCambio.abs(), egresosMesActual, egresosMesAnterior);

                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            egreso.getId(),
                            "Comparación con mes anterior",
                            mensaje);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkComparacionPeriodoEgresos: " + e.getMessage());
        }
    }

    private void checkDiaMesCritico(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int diaDelMes = now.getDayOfMonth();
            LocalDateTime inicioMes = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

            BigDecimal egresosMes = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.EGRESO, inicioMes, now);
            BigDecimal ingresosMes = movimientoRepository.calcularTotalEnRango(
                    usuarioId, TipoMovimiento.INGRESO, inicioMes, now);

            if (ingresosMes.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal porcentajeGastado = egresosMes.multiply(BigDecimal.valueOf(100))
                        .divide(ingresosMes, 2, RoundingMode.HALF_UP);

                // Alertar si estamos en día X pero ya gastamos Y%
                if (porcentajeGastado.compareTo(BigDecimal.valueOf(prefs.getAlertDiaMesCriticoPorcentaje())) >= 0) {
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            egreso.getId(),
                            "Día del mes crítico",
                            String.format(
                                    "Estamos a día %d del mes y ya gastaste el %.2f%% de tus ingresos ($%.2f de $%.2f).",
                                    diaDelMes, porcentajeGastado, egresosMes, ingresosMes));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkDiaMesCritico: " + e.getMessage());
        }
    }

    // ==================== ALERTAS DE INCONSISTENCIAS ====================

    private void checkEgresoSinConcepto(Long usuarioId, Movimiento egreso, PreferenciasFinancierasDTO prefs) {
        try {
            Long cantidadSinConcepto = movimientoRepository.countMovimientosSinConcepto(usuarioId,
                    TipoMovimiento.EGRESO);

            if (cantidadSinConcepto >= prefs.getAlertEgresoSinConceptoCantidad()) {
                notificacionService.crearNotificacion(
                        usuarioId,
                        TipoNotificacion.SISTEMA,
                        null,
                        "Egresos sin categorizar",
                        String.format(
                                "Tienes %d egresos sin concepto asignado. Categorízalos para un mejor análisis de tus gastos.",
                                cantidadSinConcepto));
            }
        } catch (Exception e) {
            System.err.println("Error en checkEgresoSinConcepto: " + e.getMessage());
        }
    }

    private void checkIngresoInusual(Long usuarioId, Movimiento ingreso, PreferenciasFinancierasDTO prefs) {
        try {
            // Calcular promedio histórico de ingresos (últimos 6 meses)
            LocalDateTime now = LocalDateTime.now();
            BigDecimal sumaHistorica = BigDecimal.ZERO;
            int contadorMeses = 0;

            for (int i = 1; i <= 6; i++) {
                LocalDateTime inicioMes = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime finMes = inicioMes.plusMonths(1).minusSeconds(1);
                BigDecimal totalMes = movimientoRepository.calcularTotalEnRango(
                        usuarioId, TipoMovimiento.INGRESO, inicioMes, finMes);

                if (totalMes.compareTo(BigDecimal.ZERO) > 0) {
                    sumaHistorica = sumaHistorica.add(totalMes);
                    contadorMeses++;
                }
            }

            if (contadorMeses > 0 && sumaHistorica.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal promedioHistorico = sumaHistorica.divide(BigDecimal.valueOf(contadorMeses), 2,
                        RoundingMode.HALF_UP);
                BigDecimal umbral = promedioHistorico
                        .multiply(BigDecimal.valueOf(prefs.getAlertIngresoInusualMultiplicador()));

                if (ingreso.getMonto().compareTo(umbral) >= 0) {
                    notificacionService.crearNotificacion(
                            usuarioId,
                            TipoNotificacion.MOVIMIENTO,
                            ingreso.getId(),
                            "Ingreso inusualmente alto",
                            String.format(
                                    "Registraste un ingreso de $%.2f, significativamente mayor a tu promedio mensual ($%.2f). Verifica que sea correcto.",
                                    ingreso.getMonto(), promedioHistorico));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en checkIngresoInusual: " + e.getMessage());
        }
    }
}
