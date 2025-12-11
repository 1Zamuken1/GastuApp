package GastuApp.Ahorro.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import GastuApp.Ahorro.Entity.AhorroMeta.Frecuencia;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EditarAhorroDTO {

    @Size(max = 100, message = "La descripción no puede superar los 100 caracteres")
    private String descripcion;

    @NotNull(message = "El monto meta es obligatorio")
    @Min(value=1, message = "El monto meta debe ser mayor a 0")
    private BigDecimal montoMeta;

    @NotNull(message = "la frecuencia es obligatoria")
    private Frecuencia frecuencia;

    @Future(message = "La fecha meta debe ser posterior a la fecha actual")
    private LocalDate fechaMeta;

    @Min(value = 1, message = "El número de cuotas debe ser mayor que 0")
    private Integer cantidadCuotas;

    public EditarAhorroDTO() {

    }

    public EditarAhorroDTO(String descripcion, BigDecimal montoMeta, Frecuencia frecuencia, LocalDate fechaMeta,
            Integer cantCuotas) {
        this.descripcion = descripcion;
        this.montoMeta = montoMeta;
        this.frecuencia = frecuencia;
        this.fechaMeta = fechaMeta;
        this.cantidadCuotas = cantCuotas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMontoMeta() {
        return montoMeta;
    }

    public void setMontoMeta(BigDecimal montoMeta) {
        this.montoMeta = montoMeta;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia) {
        this.frecuencia = frecuencia;
    }

    public LocalDate getFechaMeta() {
        return fechaMeta;
    }

    public void setFechaMeta(LocalDate fechaMeta) {
        this.fechaMeta = fechaMeta;
    }

    public Integer getCantidadCuotas() {
        return cantidadCuotas;
    }

    public void setCantidadCuotas(Integer cantidadCuotas) {
        this.cantidadCuotas = cantidadCuotas;
    }

}
