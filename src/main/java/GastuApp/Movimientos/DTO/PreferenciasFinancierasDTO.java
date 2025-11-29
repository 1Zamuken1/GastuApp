package GastuApp.Movimientos.DTO;

/**
 * DTO para preferencias financieras del usuario.
 * Define límites y alertas para el control de gastos.
 */
public class PreferenciasFinancierasDTO {

    private Integer umbralAdvertenciaPorcentaje;
    private Integer egresoGrandePorcentaje;
    private Boolean alertaEgresoGrandeActiva;

    public PreferenciasFinancierasDTO() {
    }

    public PreferenciasFinancierasDTO(Integer umbralAdvertenciaPorcentaje,
            Integer egresoGrandePorcentaje,
            Boolean alertaEgresoGrandeActiva) {
        this.umbralAdvertenciaPorcentaje = umbralAdvertenciaPorcentaje;
        this.egresoGrandePorcentaje = egresoGrandePorcentaje;
        this.alertaEgresoGrandeActiva = alertaEgresoGrandeActiva;
    }

    // Getters and Setters
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
