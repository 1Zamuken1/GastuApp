package GastuApp.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal totalAhorros;
    private BigDecimal balance;

    private String mesActual;
    private Integer anioActual;
    private Integer mesNumero; // 1-12

    // Data for Charts (last 6 months or current year)
    private List<String> chartLabels;
    private List<BigDecimal> chartIngresos;
    private List<BigDecimal> chartEgresos;
    private List<BigDecimal> chartAhorros;

    // Balance trend data (Ingresos - Egresos for each month)
    private List<BigDecimal> chartBalance;

    // Expense distribution data (Top 5 concepts + Others)
    private List<String> expenseConceptNames;
    private List<BigDecimal> expenseConceptAmounts;

    // Concept breakdown per month (for chart tooltips)
    // Map: monthIndex -> List of "conceptName: amount" strings
    private List<List<String>> ingresosConceptBreakdown;
    private List<List<String>> egresosConceptBreakdown;
    private List<List<String>> ahorrosConceptBreakdown;

    // Savings goals chart data (active goals with progress)
    private List<String> savingsGoalNames;
    private List<BigDecimal> savingsGoalTargets;
    private List<BigDecimal> savingsGoalAccumulated;

    // Budget chart data (active budgets with progress)
    private List<String> budgetNames;
    private List<BigDecimal> budgetLimits;
    private List<BigDecimal> budgetSpent;

    // Projection Chart Data (Daily breakdown)
    private List<BigDecimal> projectionRealData; // Real cumulative spend up to today
    private List<BigDecimal> projectionForecastData; // Projected spend from today to end of month
    private BigDecimal projectionLimit; // Income limit reference

    public DashboardDTO() {
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public BigDecimal getTotalEgresos() {
        return totalEgresos;
    }

    public void setTotalEgresos(BigDecimal totalEgresos) {
        this.totalEgresos = totalEgresos;
    }

    public BigDecimal getTotalAhorros() {
        return totalAhorros;
    }

    public void setTotalAhorros(BigDecimal totalAhorros) {
        this.totalAhorros = totalAhorros;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getMesActual() {
        return mesActual;
    }

    public void setMesActual(String mesActual) {
        this.mesActual = mesActual;
    }

    public Integer getAnioActual() {
        return anioActual;
    }

    public void setAnioActual(Integer anioActual) {
        this.anioActual = anioActual;
    }

    public Integer getMesNumero() {
        return mesNumero;
    }

    public void setMesNumero(Integer mesNumero) {
        this.mesNumero = mesNumero;
    }

    public List<String> getChartLabels() {
        return chartLabels;
    }

    public void setChartLabels(List<String> chartLabels) {
        this.chartLabels = chartLabels;
    }

    public List<BigDecimal> getChartIngresos() {
        return chartIngresos;
    }

    public void setChartIngresos(List<BigDecimal> chartIngresos) {
        this.chartIngresos = chartIngresos;
    }

    public List<BigDecimal> getChartEgresos() {
        return chartEgresos;
    }

    public void setChartEgresos(List<BigDecimal> chartEgresos) {
        this.chartEgresos = chartEgresos;
    }

    public List<BigDecimal> getChartAhorros() {
        return chartAhorros;
    }

    public void setChartAhorros(List<BigDecimal> chartAhorros) {
        this.chartAhorros = chartAhorros;
    }

    public List<BigDecimal> getChartBalance() {
        return chartBalance;
    }

    public void setChartBalance(List<BigDecimal> chartBalance) {
        this.chartBalance = chartBalance;
    }

    public List<String> getExpenseConceptNames() {
        return expenseConceptNames;
    }

    public void setExpenseConceptNames(List<String> expenseConceptNames) {
        this.expenseConceptNames = expenseConceptNames;
    }

    public List<BigDecimal> getExpenseConceptAmounts() {
        return expenseConceptAmounts;
    }

    public void setExpenseConceptAmounts(List<BigDecimal> expenseConceptAmounts) {
        this.expenseConceptAmounts = expenseConceptAmounts;
    }

    public List<List<String>> getIngresosConceptBreakdown() {
        return ingresosConceptBreakdown;
    }

    public void setIngresosConceptBreakdown(List<List<String>> ingresosConceptBreakdown) {
        this.ingresosConceptBreakdown = ingresosConceptBreakdown;
    }

    public List<List<String>> getEgresosConceptBreakdown() {
        return egresosConceptBreakdown;
    }

    public void setEgresosConceptBreakdown(List<List<String>> egresosConceptBreakdown) {
        this.egresosConceptBreakdown = egresosConceptBreakdown;
    }

    public List<List<String>> getAhorrosConceptBreakdown() {
        return ahorrosConceptBreakdown;
    }

    public void setAhorrosConceptBreakdown(List<List<String>> ahorrosConceptBreakdown) {
        this.ahorrosConceptBreakdown = ahorrosConceptBreakdown;
    }

    public List<String> getSavingsGoalNames() {
        return savingsGoalNames;
    }

    public void setSavingsGoalNames(List<String> savingsGoalNames) {
        this.savingsGoalNames = savingsGoalNames;
    }

    public List<BigDecimal> getSavingsGoalTargets() {
        return savingsGoalTargets;
    }

    public void setSavingsGoalTargets(List<BigDecimal> savingsGoalTargets) {
        this.savingsGoalTargets = savingsGoalTargets;
    }

    public List<BigDecimal> getSavingsGoalAccumulated() {
        return savingsGoalAccumulated;
    }

    public void setSavingsGoalAccumulated(List<BigDecimal> savingsGoalAccumulated) {
        this.savingsGoalAccumulated = savingsGoalAccumulated;
    }

    public List<String> getBudgetNames() {
        return budgetNames;
    }

    public void setBudgetNames(List<String> budgetNames) {
        this.budgetNames = budgetNames;
    }

    public List<BigDecimal> getBudgetLimits() {
        return budgetLimits;
    }

    public void setBudgetLimits(List<BigDecimal> budgetLimits) {
        this.budgetLimits = budgetLimits;
    }

    public List<BigDecimal> getBudgetSpent() {
        return budgetSpent;
    }

    public void setBudgetSpent(List<BigDecimal> budgetSpent) {
        this.budgetSpent = budgetSpent;
    }

    public List<BigDecimal> getProjectionRealData() {
        return projectionRealData;
    }

    public void setProjectionRealData(List<BigDecimal> projectionRealData) {
        this.projectionRealData = projectionRealData;
    }

    public List<BigDecimal> getProjectionForecastData() {
        return projectionForecastData;
    }

    public void setProjectionForecastData(List<BigDecimal> projectionForecastData) {
        this.projectionForecastData = projectionForecastData;
    }

    public BigDecimal getProjectionLimit() {
        return projectionLimit;
    }

    public void setProjectionLimit(BigDecimal projectionLimit) {
        this.projectionLimit = projectionLimit;
    }
}
