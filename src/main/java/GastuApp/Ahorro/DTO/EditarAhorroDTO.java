package GastuApp.Ahorro.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import GastuApp.Ahorro.Entity.AhorroMeta.Frecuencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EditarAhorroDTO {
    
    @Size(max=100, message = "La descripción no puede superar los 100 caracteres")
    private String descripcion;

    @NotNull(message = "El monto meta es obligatorio")
    private BigDecimal montoMeta;

    @NotNull(message = "la frecuencia es obligatoria")
    private Frecuencia frecuencia;

    private LocalDate fechaMeta;

    private Integer cantidadCuotas;

    public EditarAhorroDTO(){

    }

    public EditarAhorroDTO(String descripcion, BigDecimal montoMeta, Frecuencia frecuencia, LocalDate fechaMeta, Integer cantCuotas){
        this.descripcion=descripcion;
        this.montoMeta=montoMeta;
        this.frecuencia=frecuencia;
        this.fechaMeta=fechaMeta;
        this.cantidadCuotas=cantCuotas;
    }

    public String getDescripcion(){
        return descripcion;
    }

    public void setDescripcion(String descripcion){
        this.descripcion= descripcion;
    }

    public BigDecimal getMonto(){
        return montoMeta;
    }
    
    public void setMonto(BigDecimal montoMeta){
        this.montoMeta= montoMeta;
    }

    public Frecuencia getFrecuencia(){
        return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia){
        this.frecuencia=frecuencia;
    }

    public LocalDate getMeta(){
        return fechaMeta;
    }

    public void setMeta(LocalDate fechaMeta){
        this.fechaMeta=fechaMeta;
    }

    public Integer getCantCuotas(){
        return cantidadCuotas;
    }

    public void setCantCuotas( Integer cantidadCuotas){
        this.cantidadCuotas= cantidadCuotas;
    }

}
