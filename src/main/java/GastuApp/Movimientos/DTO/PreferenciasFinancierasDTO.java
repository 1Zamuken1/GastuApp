package GastuApp.Movimientos.DTO;

import jakarta.validation.constraints.*;

/**
 * DTO para configuración de preferencias financieras del usuario.
 * Permite personalizar los umbrales de validación de salud financiera.
 */
public class PreferenciasFinancierasDTO {

    /**
     * Porcentaje de umbral de advertencia (1-100).
     * Cuando los egresos alcanzan este porcentaje de los ingresos, se genera una
     * notificación.
     * Ejemplo: 80 significa que se notificará cuando egresos >= 80% de ingresos.
     */
    @NotNull(message = "El umbral de advertencia es obligatorio")
    @Min(value = 1, message = "El umbral de advertencia debe ser al menos 1%")
    @Max(value = 100, message = "El umbral de advertencia no puede exceder 100%")
    private Integer umbralAdvertenciaPorcentaje;

    /**
     * Porcentaje para considerar un egreso individual como "grande" (1-100).
     * Cuando un egreso representa este porcentaje o más de los ingresos totales, se
     * genera una notificación.
     * Ejemplo: 30 significa que se notificará cuando un egreso >= 30% de ingresos
     * totales.
     */
    @NotNull(message = "El porcentaje de egreso grande es obligatorio")
    @Min(value = 1, message = "El porcentaje de egreso grande debe ser al menos 1%")
    @Max(value = 100, message = "El porcentaje de egreso grande no puede exceder 100%")
    private Integer egresoGrandePorcentaje;

    /**
     * Indica si la alerta de egreso individual grande está activa.
     * Si es false, no se generarán notificaciones por egresos individuales grandes.
     */
    @NotNull(message = "El estado de alerta de egreso grande es obligatorio")
    private Boolean alertaEgresoGrandeActiva;

    // Constructores
    public PreferenciasFinancierasDTO() {
    }

    public PreferenciasFinancierasDTO(Integer umbralAdvertenciaPorcentaje,
            Integer egresoGrandePorcentaje,
            Boolean alertaEgresoGrandeActiva) {
        this.umbralAdvertenciaPorcentaje = umbralAdvertenciaPorcentaje;
        this.egresoGrandePorcentaje = egresoGrandePorcentaje;
        this.alertaEgresoGrandeActiva = alertaEgresoGrandeActiva;
    }

    // Getters y Setters
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
}
