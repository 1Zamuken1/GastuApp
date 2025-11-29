package GastuApp.Movimientos.DTO;

import java.math.BigDecimal;

/**
 * DTO para mostrar un resumen de un concepto con estadísticas de uso.
 * Utilizado en la vista principal de Ingresos/Egresos.
 */
public class ConceptoResumenDTO {
    private Long conceptoId;
    private String nombre;
    private String descripcion;
    private Long cantidadRegistros;
    private BigDecimal totalAcumulado;

    public ConceptoResumenDTO() {
    }

    public ConceptoResumenDTO(Long conceptoId, String nombre, String descripcion, Long cantidadRegistros,
            BigDecimal totalAcumulado) {
        this.conceptoId = conceptoId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidadRegistros = cantidadRegistros;
        this.totalAcumulado = totalAcumulado;
    }

    public Long getConceptoId() {
        return conceptoId;
    }

    public void setConceptoId(Long conceptoId) {
        this.conceptoId = conceptoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getCantidadRegistros() {
        return cantidadRegistros;
    }

    public void setCantidadRegistros(Long cantidadRegistros) {
        this.cantidadRegistros = cantidadRegistros;
    }

    public BigDecimal getTotalAcumulado() {
        return totalAcumulado;
    }

    public void setTotalAcumulado(BigDecimal totalAcumulado) {
        this.totalAcumulado = totalAcumulado;
    }
}
