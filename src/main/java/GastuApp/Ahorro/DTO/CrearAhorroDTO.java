package GastuApp.Ahorro.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import GastuApp.Ahorro.Entity.AhorroMeta.Frecuencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CrearAhorroDTO {
    
    @NotNull(message = "El concepto es obligatorio")
    private Long conceptoId;

    @Size(max=100, message = "La descripción no puede superar los 100 caracteres")
    private String descripcion;


    private BigDecimal montoMeta;

    @NotNull(message = "la frecuencia es obligatoria")
    private Frecuencia frecuencia;

    private LocalDate fechaMeta;

    private Integer cantidadCuotas;

    public CrearAhorroDTO(){

    }

    public CrearAhorroDTO(Long concepto, String descripcion, BigDecimal montoMeta, Frecuencia frecuencia, LocalDate fechaMeta, Integer cantCuotas){
        this.conceptoId=concepto;
        this.descripcion=descripcion;
        this.montoMeta=montoMeta;
        this.frecuencia=frecuencia;
        this.fechaMeta=fechaMeta;
        this.cantidadCuotas=cantCuotas;
    }

    public Long getConceptoId(){
        return conceptoId;
    }

    public void setConceptoId(Long conceptoId){
        this.conceptoId=conceptoId;
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
