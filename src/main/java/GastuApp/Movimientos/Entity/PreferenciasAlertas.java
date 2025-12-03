package GastuApp.Movimientos.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad JPA para almacenar las preferencias de alertas del usuario.
 * Esta tabla es independiente y no modifica estructuras existentes.
 */
@Entity
@Table(name = "preferencias_alertas")
public class PreferenciasAlertas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    // ==================== ALERTAS EXISTENTES ====================
    @Column(name = "umbral_advertencia_porcentaje")
    private Integer umbralAdvertenciaPorcentaje = 80;

    @Column(name = "egreso_grande_porcentaje")
    private Integer egresoGrandePorcentaje = 30;

    @Column(name = "alerta_egreso_grande_activa")
    private Boolean alertaEgresoGrandeActiva = true;

    // ==================== ALERTAS BASADAS EN TENDENCIAS ====================
    @Column(name = "alert_gasto_incremental_enabled")
    private Boolean alertGastoIncrementalEnabled = true;

    @Column(name = "alert_gasto_incremental_porcentaje")
    private Integer alertGastoIncrementalPorcentaje = 25; // % de incremento vs promedio histórico

    @Column(name = "alert_gasto_incremental_meses")
    private Integer alertGastoIncrementalMeses = 3; // Comparar con promedio de últimos N meses

    @Column(name = "alert_reduccion_ingresos_enabled")
    private Boolean alertReduccionIngresosEnabled = true;

    @Column(name = "alert_reduccion_ingresos_porcentaje")
    private Integer alertReduccionIngresosPorcentaje = 20; // % de reducción vs promedio

    @Column(name = "alert_patron_inusual_enabled")
    private Boolean alertPatronInusualEnabled = true;

    // ==================== ALERTAS BASADAS EN CONCEPTOS ====================
    @Column(name = "alert_concentracion_gastos_enabled")
    private Boolean alertConcentracionGastosEnabled = true;

    @Column(name = "alert_concentracion_gastos_porcentaje")
    private Integer alertConcentracionGastosPorcentaje = 50; // Un solo concepto representa X% del total

    @Column(name = "alert_concepto_sin_uso_enabled")
    private Boolean alertConceptoSinUsoEnabled = false; // Deshabilitada por defecto

    @Column(name = "alert_concepto_sin_uso_dias")
    private Integer alertConceptoSinUsoDias = 30; // Días sin registrar concepto recurrente

    // ==================== ALERTAS BASADAS EN TIEMPO ====================
    @Column(name = "alert_velocidad_gasto_enabled")
    private Boolean alertVelocidadGastoEnabled = true;

    @Column(name = "alert_inactividad_ingresos_enabled")
    private Boolean alertInactividadIngresosEnabled = true;

    @Column(name = "alert_inactividad_dias")
    private Integer alertInactividadDias = 7; // Días sin registrar ingresos

    @Column(name = "alert_egresos_agrupados_enabled")
    private Boolean alertEgresosAgrupadosEnabled = true;

    @Column(name = "alert_egresos_agrupados_cantidad")
    private Integer alertEgresosAgrupadosCantidad = 5; // N transacciones en periodo corto

    @Column(name = "alert_egresos_agrupados_horas")
    private Integer alertEgresosAgrupadosHoras = 2; // Periodo en horas

    // ==================== ALERTAS DE AHORRO/BALANCE ====================
    @Column(name = "meta_ahorro_mensual", precision = 12, scale = 2)
    private BigDecimal metaAhorroMensual = BigDecimal.ZERO;

    @Column(name = "alert_meta_ahorro_enabled")
    private Boolean alertMetaAhorroEnabled = false; // Usuario debe configurar meta primero

    @Column(name = "alert_balance_critico_enabled")
    private Boolean alertBalanceCriticoEnabled = true;

    // ==================== ALERTAS DE "SANGRÍA" (Micro-gastos) ====================
    @Column(name = "alert_micro_gastos_enabled")
    private Boolean alertMicroGastosEnabled = true;

    @Column(name = "alert_micro_gastos_cantidad")
    private Integer alertMicroGastosCantidad = 10; // Cantidad de gastos pequeños

    @Column(name = "alert_micro_gastos_monto_max", precision = 12, scale = 2)
    private BigDecimal alertMicroGastosMontoMax = new BigDecimal("5.00"); // Monto máximo para considerar "micro"

    @Column(name = "alert_gastos_hormiga_enabled")
    private Boolean alertGastosHormigaEnabled = true;

    @Column(name = "alert_gastos_hormiga_monto_max", precision = 12, scale = 2)
    private BigDecimal alertGastosHormigaMontoMax = new BigDecimal("10.00"); // Umbral diario

    // ==================== ALERTAS PREDICTIVAS/PROACTIVAS ====================
    @Column(name = "alert_proyeccion_sobregasto_enabled")
    private Boolean alertProyeccionSobregastoEnabled = true;

    @Column(name = "alert_comparacion_periodo_enabled")
    private Boolean alertComparacionPeriodoEnabled = true;

    @Column(name = "alert_dia_mes_critico_enabled")
    private Boolean alertDiaMesCriticoEnabled = true;

    @Column(name = "alert_dia_mes_critico_porcentaje")
    private Integer alertDiaMesCriticoPorcentaje = 70; // % gastado vs día del mes

    // ==================== ALERTAS DE INCONSISTENCIAS ====================
    @Column(name = "alert_egreso_sin_concepto_enabled")
    private Boolean alertEgresoSinConceptoEnabled = false; // Opcional

    @Column(name = "alert_egreso_sin_concepto_cantidad")
    private Integer alertEgresoSinConceptoCantidad = 5; // N movimientos sin categorizar

    @Column(name = "alert_ingreso_inusual_enabled")
    private Boolean alertIngresoInusualEnabled = true;

    @Column(name = "alert_ingreso_inusual_multiplicador")
    private Integer alertIngresoInusualMultiplicador = 2; // Ingreso X veces mayor al promedio

    // Constructores
    public PreferenciasAlertas() {
    }

    public PreferenciasAlertas(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getUmbralAdvertenciaPorcentaje() {
        return umbralAdvertenciaPorcentaje;
    }

    public void setUmbralAdvertenciaPorcentaje(Integer umbralAdvertenciaPorcentaje) {
        this.umbralAdvertenciaPorcentaje = umbralAdvertenciaPorcentaje;
    }

    public Integer getEgresoGrandePorcentaje() {
        return egresoGrandePorcentaje;
    }

    public void setEgresoGrandePorcentaje(Integer egresoGrandePorcentaje) {
        this.egresoGrandePorcentaje = egresoGrandePorcentaje;
    }

    public Boolean getAlertaEgresoGrandeActiva() {
        return alertaEgresoGrandeActiva;
    }

    public void setAlertaEgresoGrandeActiva(Boolean alertaEgresoGrandeActiva) {
        this.alertaEgresoGrandeActiva = alertaEgresoGrandeActiva;
    }

    public Boolean getAlertGastoIncrementalEnabled() {
        return alertGastoIncrementalEnabled;
    }

    public void setAlertGastoIncrementalEnabled(Boolean alertGastoIncrementalEnabled) {
        this.alertGastoIncrementalEnabled = alertGastoIncrementalEnabled;
    }

    public Integer getAlertGastoIncrementalPorcentaje() {
        return alertGastoIncrementalPorcentaje;
    }

    public void setAlertGastoIncrementalPorcentaje(Integer alertGastoIncrementalPorcentaje) {
        this.alertGastoIncrementalPorcentaje = alertGastoIncrementalPorcentaje;
    }

    public Integer getAlertGastoIncrementalMeses() {
        return alertGastoIncrementalMeses;
    }

    public void setAlertGastoIncrementalMeses(Integer alertGastoIncrementalMeses) {
        this.alertGastoIncrementalMeses = alertGastoIncrementalMeses;
    }

    public Boolean getAlertReduccionIngresosEnabled() {
        return alertReduccionIngresosEnabled;
    }

    public void setAlertReduccionIngresosEnabled(Boolean alertReduccionIngresosEnabled) {
        this.alertReduccionIngresosEnabled = alertReduccionIngresosEnabled;
    }

    public Integer getAlertReduccionIngresosPorcentaje() {
        return alertReduccionIngresosPorcentaje;
    }

    public void setAlertReduccionIngresosPorcentaje(Integer alertReduccionIngresosPorcentaje) {
        this.alertReduccionIngresosPorcentaje = alertReduccionIngresosPorcentaje;
    }

    public Boolean getAlertPatronInusualEnabled() {
        return alertPatronInusualEnabled;
    }

    public void setAlertPatronInusualEnabled(Boolean alertPatronInusualEnabled) {
        this.alertPatronInusualEnabled = alertPatronInusualEnabled;
    }

    public Boolean getAlertConcentracionGastosEnabled() {
        return alertConcentracionGastosEnabled;
    }

    public void setAlertConcentracionGastosEnabled(Boolean alertConcentracionGastosEnabled) {
        this.alertConcentracionGastosEnabled = alertConcentracionGastosEnabled;
    }

    public Integer getAlertConcentracionGastosPorcentaje() {
        return alertConcentracionGastosPorcentaje;
    }

    public void setAlertConcentracionGastosPorcentaje(Integer alertConcentracionGastosPorcentaje) {
        this.alertConcentracionGastosPorcentaje = alertConcentracionGastosPorcentaje;
    }

    public Boolean getAlertConceptoSinUsoEnabled() {
        return alertConceptoSinUsoEnabled;
    }

    public void setAlertConceptoSinUsoEnabled(Boolean alertConceptoSinUsoEnabled) {
        this.alertConceptoSinUsoEnabled = alertConceptoSinUsoEnabled;
    }

    public Integer getAlertConceptoSinUsoDias() {
        return alertConceptoSinUsoDias;
    }

    public void setAlertConceptoSinUsoDias(Integer alertConceptoSinUsoDias) {
        this.alertConceptoSinUsoDias = alertConceptoSinUsoDias;
    }

    public Boolean getAlertVelocidadGastoEnabled() {
        return alertVelocidadGastoEnabled;
    }

    public void setAlertVelocidadGastoEnabled(Boolean alertVelocidadGastoEnabled) {
        this.alertVelocidadGastoEnabled = alertVelocidadGastoEnabled;
    }

    public Boolean getAlertInactividadIngresosEnabled() {
        return alertInactividadIngresosEnabled;
    }

    public void setAlertInactividadIngresosEnabled(Boolean alertInactividadIngresosEnabled) {
        this.alertInactividadIngresosEnabled = alertInactividadIngresosEnabled;
    }

    public Integer getAlertInactividadDias() {
        return alertInactividadDias;
    }

    public void setAlertInactividadDias(Integer alertInactividadDias) {
        this.alertInactividadDias = alertInactividadDias;
    }

    public Boolean getAlertEgresosAgrupadosEnabled() {
        return alertEgresosAgrupadosEnabled;
    }

    public void setAlertEgresosAgrupadosEnabled(Boolean alertEgresosAgrupadosEnabled) {
        this.alertEgresosAgrupadosEnabled = alertEgresosAgrupadosEnabled;
    }

    public Integer getAlertEgresosAgrupadosCantidad() {
        return alertEgresosAgrupadosCantidad;
    }

    public void setAlertEgresosAgrupadosCantidad(Integer alertEgresosAgrupadosCantidad) {
        this.alertEgresosAgrupadosCantidad = alertEgresosAgrupadosCantidad;
    }

    public Integer getAlertEgresosAgrupadosHoras() {
        return alertEgresosAgrupadosHoras;
    }

    public void setAlertEgresosAgrupadosHoras(Integer alertEgresosAgrupadosHoras) {
        this.alertEgresosAgrupadosHoras = alertEgresosAgrupadosHoras;
    }

    public BigDecimal getMetaAhorroMensual() {
        return metaAhorroMensual;
    }

    public void setMetaAhorroMensual(BigDecimal metaAhorroMensual) {
        this.metaAhorroMensual = metaAhorroMensual;
    }

    public Boolean getAlertMetaAhorroEnabled() {
        return alertMetaAhorroEnabled;
    }

    public void setAlertMetaAhorroEnabled(Boolean alertMetaAhorroEnabled) {
        this.alertMetaAhorroEnabled = alertMetaAhorroEnabled;
    }

    public Boolean getAlertBalanceCriticoEnabled() {
        return alertBalanceCriticoEnabled;
    }

    public void setAlertBalanceCriticoEnabled(Boolean alertBalanceCriticoEnabled) {
        this.alertBalanceCriticoEnabled = alertBalanceCriticoEnabled;
    }

    public Boolean getAlertMicroGastosEnabled() {
        return alertMicroGastosEnabled;
    }

    public void setAlertMicroGastosEnabled(Boolean alertMicroGastosEnabled) {
        this.alertMicroGastosEnabled = alertMicroGastosEnabled;
    }

    public Integer getAlertMicroGastosCantidad() {
        return alertMicroGastosCantidad;
    }

    public void setAlertMicroGastosCantidad(Integer alertMicroGastosCantidad) {
        this.alertMicroGastosCantidad = alertMicroGastosCantidad;
    }

    public BigDecimal getAlertMicroGastosMontoMax() {
        return alertMicroGastosMontoMax;
    }

    public void setAlertMicroGastosMontoMax(BigDecimal alertMicroGastosMontoMax) {
        this.alertMicroGastosMontoMax = alertMicroGastosMontoMax;
    }

    public Boolean getAlertGastosHormigaEnabled() {
        return alertGastosHormigaEnabled;
    }

    public void setAlertGastosHormigaEnabled(Boolean alertGastosHormigaEnabled) {
        this.alertGastosHormigaEnabled = alertGastosHormigaEnabled;
    }

    public BigDecimal getAlertGastosHormigaMontoMax() {
        return alertGastosHormigaMontoMax;
    }

    public void setAlertGastosHormigaMontoMax(BigDecimal alertGastosHormigaMontoMax) {
        this.alertGastosHormigaMontoMax = alertGastosHormigaMontoMax;
    }

    public Boolean getAlertProyeccionSobregastoEnabled() {
        return alertProyeccionSobregastoEnabled;
    }

    public void setAlertProyeccionSobregastoEnabled(Boolean alertProyeccionSobregastoEnabled) {
        this.alertProyeccionSobregastoEnabled = alertProyeccionSobregastoEnabled;
    }

    public Boolean getAlertComparacionPeriodoEnabled() {
        return alertComparacionPeriodoEnabled;
    }

    public void setAlertComparacionPeriodoEnabled(Boolean alertComparacionPeriodoEnabled) {
        this.alertComparacionPeriodoEnabled = alertComparacionPeriodoEnabled;
    }

    public Boolean getAlertDiaMesCriticoEnabled() {
        return alertDiaMesCriticoEnabled;
    }

    public void setAlertDiaMesCriticoEnabled(Boolean alertDiaMesCriticoEnabled) {
        this.alertDiaMesCriticoEnabled = alertDiaMesCriticoEnabled;
    }

    public Integer getAlertDiaMesCriticoPorcentaje() {
        return alertDiaMesCriticoPorcentaje;
    }

    public void setAlertDiaMesCriticoPorcentaje(Integer alertDiaMesCriticoPorcentaje) {
        this.alertDiaMesCriticoPorcentaje = alertDiaMesCriticoPorcentaje;
    }

    public Boolean getAlertEgresoSinConceptoEnabled() {
        return alertEgresoSinConceptoEnabled;
    }

    public void setAlertEgresoSinConceptoEnabled(Boolean alertEgresoSinConceptoEnabled) {
        this.alertEgresoSinConceptoEnabled = alertEgresoSinConceptoEnabled;
    }

    public Integer getAlertEgresoSinConceptoCantidad() {
        return alertEgresoSinConceptoCantidad;
    }

    public void setAlertEgresoSinConceptoCantidad(Integer alertEgresoSinConceptoCantidad) {
        this.alertEgresoSinConceptoCantidad = alertEgresoSinConceptoCantidad;
    }

    public Boolean getAlertIngresoInusualEnabled() {
        return alertIngresoInusualEnabled;
    }

    public void setAlertIngresoInusualEnabled(Boolean alertIngresoInusualEnabled) {
        this.alertIngresoInusualEnabled = alertIngresoInusualEnabled;
    }

    public Integer getAlertIngresoInusualMultiplicador() {
        return alertIngresoInusualMultiplicador;
    }

    public void setAlertIngresoInusualMultiplicador(Integer alertIngresoInusualMultiplicador) {
        this.alertIngresoInusualMultiplicador = alertIngresoInusualMultiplicador;
    }
}
