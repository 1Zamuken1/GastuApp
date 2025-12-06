package GastuApp.Service;

import GastuApp.Ahorro.Entity.AhorroMeta;
import GastuApp.Ahorro.Repository.AhorroMetaRepository;
import GastuApp.Ahorro.Repository.AporteAhorroRepository;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.DTO.DashboardDTO;
import GastuApp.Movimientos.Entity.Movimiento.TipoMovimiento;
import GastuApp.Movimientos.Repository.MovimientoRepository;
import GastuApp.Planificacion.DTO.PresupuestoDTO;
import GastuApp.Planificacion.Service.PresupuestoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {

    private final MovimientoRepository movimientoRepository;
    private final AporteAhorroRepository aporteAhorroRepository;
    private final AhorroMetaRepository ahorroMetaRepository;
    private final ConceptoService conceptoService;
    private final PresupuestoService presupuestoService;

    public DashboardService(MovimientoRepository movimientoRepository,
            AporteAhorroRepository aporteAhorroRepository,
            AhorroMetaRepository ahorroMetaRepository,
            ConceptoService conceptoService,
            PresupuestoService presupuestoService) {
        this.movimientoRepository = movimientoRepository;
        this.aporteAhorroRepository = aporteAhorroRepository;
        this.ahorroMetaRepository = ahorroMetaRepository;
        this.conceptoService = conceptoService;
        this.presupuestoService = presupuestoService;
    }

    @Transactional(readOnly = true)
    public DashboardDTO obtenerDatosDashboard(Long usuarioId, Integer mes, Integer anio) {
        DashboardDTO dto = new DashboardDTO();

        // Defaults to current date if null
        LocalDate now = LocalDate.now();
        int mesActual = (mes != null) ? mes : now.getMonthValue();
        int anioActual = (anio != null) ? anio : now.getYear();

        dto.setMesNumero(mesActual);
        dto.setAnioActual(anioActual);
        dto.setMesActual(Month.of(mesActual).getDisplayName(TextStyle.FULL, new Locale("es", "ES")));

        // 1. Totals for the selected month
        LocalDateTime inicioMes = LocalDate.of(anioActual, mesActual, 1).atStartOfDay();
        LocalDateTime finMes = inicioMes.plusMonths(1).minusSeconds(1);

        BigDecimal ingresos = movimientoRepository.calcularTotalEnRango(usuarioId, TipoMovimiento.INGRESO, inicioMes,
                finMes);
        BigDecimal egresos = movimientoRepository.calcularTotalEnRango(usuarioId, TipoMovimiento.EGRESO, inicioMes,
                finMes);

        // Savings: sum "APORTADO" contributions with fechaLimite in this month
        BigDecimal ahorros = aporteAhorroRepository.sumarAportesPorUsuarioYRango(usuarioId, inicioMes.toLocalDate(),
                finMes.toLocalDate());

        dto.setTotalIngresos(ingresos != null ? ingresos : BigDecimal.ZERO);
        dto.setTotalEgresos(egresos != null ? egresos : BigDecimal.ZERO);
        dto.setTotalAhorros(ahorros != null ? ahorros : BigDecimal.ZERO);

        // Balance = Ingresos - Egresos - Ahorros
        // Los ahorros se restan porque es dinero comprometido que no está disponible
        BigDecimal ahorrosVal = ahorros != null ? ahorros : BigDecimal.ZERO;
        dto.setBalance(dto.getTotalIngresos().subtract(dto.getTotalEgresos()).subtract(ahorrosVal));

        // 2. Chart Data (Daily within selected month) - OPTIMIZED
        List<String> labels = new ArrayList<>();
        List<BigDecimal> dataIngresos = new ArrayList<>();
        List<BigDecimal> dataEgresos = new ArrayList<>();
        List<BigDecimal> dataAhorros = new ArrayList<>();
        List<BigDecimal> dataBalance = new ArrayList<>();

        // Concept breakdown lists (kept empty/simple for performance)
        List<List<String>> ingresosBreakdown = new ArrayList<>();
        List<List<String>> egresosBreakdown = new ArrayList<>();
        List<List<String>> ahorrosBreakdown = new ArrayList<>();

        // Fetch all daily totals in batch (3 queries total instead of 3 * 31)
        List<Object[]> ingresosDiarios = movimientoRepository.obtenerTotalesDiariosPorTipo(
                usuarioId, TipoMovimiento.INGRESO.name(), inicioMes, finMes);
        List<Object[]> egresosDiarios = movimientoRepository.obtenerTotalesDiariosPorTipo(
                usuarioId, TipoMovimiento.EGRESO.name(), inicioMes, finMes);
        List<Object[]> ahorrosDiarios = aporteAhorroRepository.obtenerAportesDiariosPorUsuarioYRango(
                usuarioId, inicioMes.toLocalDate(), finMes.toLocalDate());

        // Map results to Day -> Amount for O(1) access
        java.util.Map<Integer, BigDecimal> mapIngresos = new java.util.HashMap<>();
        for (Object[] r : ingresosDiarios)
            mapIngresos.put(((Number) r[0]).intValue(), (BigDecimal) r[1]);

        java.util.Map<Integer, BigDecimal> mapEgresos = new java.util.HashMap<>();
        for (Object[] r : egresosDiarios)
            mapEgresos.put(((Number) r[0]).intValue(), (BigDecimal) r[1]);

        java.util.Map<Integer, BigDecimal> mapAhorros = new java.util.HashMap<>();
        for (Object[] r : ahorrosDiarios)
            mapAhorros.put(((Number) r[0]).intValue(), (BigDecimal) r[1]);

        // Get number of days in the selected month
        int daysInMonth = inicioMes.toLocalDate().lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            // Label: just the day number
            labels.add(String.valueOf(day));

            BigDecimal ing = mapIngresos.getOrDefault(day, BigDecimal.ZERO);
            BigDecimal egr = mapEgresos.getOrDefault(day, BigDecimal.ZERO);
            BigDecimal aho = mapAhorros.getOrDefault(day, BigDecimal.ZERO);

            dataIngresos.add(ing);
            dataEgresos.add(egr);
            dataAhorros.add(aho);

            // Balance = Ingresos - Egresos - Ahorros
            BigDecimal balanceDia = ing.subtract(egr).subtract(aho);
            dataBalance.add(balanceDia);

            // Add simple generic breakdown or empty to save 60+ queries
            // To restore simple details without extra queries, we could fetch all movements
            // list
            // but for now, performance is the priority.
            if (ing.compareTo(BigDecimal.ZERO) > 0) {
                ingresosBreakdown.add(Collections.singletonList("Ver detalles en la lista"));
            } else {
                ingresosBreakdown.add(Collections.emptyList());
            }

            if (egr.compareTo(BigDecimal.ZERO) > 0) {
                egresosBreakdown.add(Collections.singletonList("Ver detalles en la lista"));
            } else {
                egresosBreakdown.add(Collections.emptyList());
            }

            if (aho.compareTo(BigDecimal.ZERO) > 0) {
                ahorrosBreakdown.add(Collections.singletonList("Aportes del día"));
            } else {
                ahorrosBreakdown.add(Collections.emptyList());
            }
        }

        dto.setChartLabels(labels);
        dto.setChartIngresos(dataIngresos);
        dto.setChartEgresos(dataEgresos);
        dto.setChartAhorros(dataAhorros);
        dto.setChartBalance(dataBalance);
        dto.setIngresosConceptBreakdown(ingresosBreakdown);
        dto.setEgresosConceptBreakdown(egresosBreakdown);
        dto.setAhorrosConceptBreakdown(ahorrosBreakdown);

        // 3. Expense Distribution (Top 5 concepts + Others for current month)
        List<Object[]> expenseStats = movimientoRepository.obtenerEstadisticasPorConceptoEnRango(
                usuarioId, TipoMovimiento.EGRESO, inicioMes, finMes);

        List<String> conceptNames = new ArrayList<>();
        List<BigDecimal> conceptAmounts = new ArrayList<>();

        if (expenseStats != null && !expenseStats.isEmpty()) {
            // Sort by amount descending
            expenseStats.sort((a, b) -> {
                BigDecimal amountA = (BigDecimal) a[2];
                BigDecimal amountB = (BigDecimal) b[2];
                return amountB.compareTo(amountA);
            });

            BigDecimal othersTotal = BigDecimal.ZERO;
            int maxConcepts = Math.min(5, expenseStats.size());

            for (int i = 0; i < expenseStats.size(); i++) {
                Object[] stat = expenseStats.get(i);
                Long conceptoId = (Long) stat[0];
                BigDecimal amount = (BigDecimal) stat[2];

                if (i < maxConcepts) {
                    // Get concept name
                    try {
                        String conceptName = conceptoService.obtenerPorId(conceptoId).getNombre();
                        conceptNames.add(conceptName);
                        conceptAmounts.add(amount);
                    } catch (Exception e) {
                        conceptNames.add("Concepto #" + conceptoId);
                        conceptAmounts.add(amount);
                    }
                } else {
                    // Add to "Others"
                    othersTotal = othersTotal.add(amount);
                }
            }

            // Add "Others" if there are more than 5 concepts
            if (expenseStats.size() > maxConcepts && othersTotal.compareTo(BigDecimal.ZERO) > 0) {
                conceptNames.add("Otros");
                conceptAmounts.add(othersTotal);
            }
        }

        dto.setExpenseConceptNames(conceptNames);
        dto.setExpenseConceptAmounts(conceptAmounts);

        // 4. Savings Goals (active goals with progress)
        List<String> goalNames = new ArrayList<>();
        List<BigDecimal> goalTargets = new ArrayList<>();
        List<BigDecimal> goalAccumulated = new ArrayList<>();

        List<AhorroMeta> activeGoals = ahorroMetaRepository.findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(
                usuarioId, AhorroMeta.Estado.ACTIVO);

        for (AhorroMeta goal : activeGoals) {
            try {
                String conceptName = conceptoService.obtenerPorId(goal.getConceptoId()).getNombre();
                goalNames.add(conceptName);
            } catch (Exception e) {
                goalNames.add(goal.getDescripcion() != null ? goal.getDescripcion() : "Meta #" + goal.getId());
            }
            goalTargets.add(goal.getMonto() != null ? goal.getMonto() : BigDecimal.ZERO);
            goalAccumulated.add(goal.getAcumulado() != null ? goal.getAcumulado() : BigDecimal.ZERO);
        }

        dto.setSavingsGoalNames(goalNames);
        dto.setSavingsGoalTargets(goalTargets);
        dto.setSavingsGoalAccumulated(goalAccumulated);

        // 5. Budget Progress (active budgets with spending)
        List<String> budgetNames = new ArrayList<>();
        List<BigDecimal> budgetLimits = new ArrayList<>();
        List<BigDecimal> budgetSpent = new ArrayList<>();

        List<PresupuestoDTO> budgets = presupuestoService.obtenerPresupuestosConProgreso(usuarioId);
        for (PresupuestoDTO budget : budgets) {
            if (budget.getActivo() != null && budget.getActivo()) {
                budgetNames.add(budget.getConceptoNombre() != null ? budget.getConceptoNombre()
                        : "Presupuesto #" + budget.getId());
                budgetLimits.add(budget.getLimite() != null ? budget.getLimite() : BigDecimal.ZERO);
                budgetSpent
                        .add(budget.getGastado() != null ? BigDecimal.valueOf(budget.getGastado()) : BigDecimal.ZERO);
            }
        }

        dto.setBudgetNames(budgetNames);
        dto.setBudgetLimits(budgetLimits);
        dto.setBudgetSpent(budgetSpent);

        return dto;
    }
}
