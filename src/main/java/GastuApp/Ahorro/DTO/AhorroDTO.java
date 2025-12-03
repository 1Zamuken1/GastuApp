package GastuApp.Ahorro.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import GastuApp.Ahorro.Entity.AhorroMeta.Estado;
import GastuApp.Ahorro.Entity.AhorroMeta.Frecuencia;

public class AhorroDTO {
    
    private Long ahorroId;
    private Long conceptoId;
    private String descripcion;
    private BigDecimal montoMeta;
    private BigDecimal totalAcumulado;
    private Frecuencia frecuencia;
    private LocalDate fechaCreacion;
    private LocalDate fechaMeta;
    private Estado estado;
    private Integer cantidadCuotas;

    public AhorroDTO(){

    }

    public AhorroDTO(Long ahorro,Long concepto, String descripcion, BigDecimal montoMeta, BigDecimal acumulado, Frecuencia frecuencia, LocalDate fechaCreacion, LocalDate fechaMeta, Estado estado, Integer cantCuotas){
        this.ahorroId=ahorro;
        this.conceptoId=concepto;
        this.descripcion=descripcion;
        this.montoMeta=montoMeta;
        this.totalAcumulado=acumulado;
        this.frecuencia=frecuencia;
        this.fechaCreacion=fechaCreacion;
        this.fechaMeta=fechaMeta;
        this.estado=estado;
        this.cantidadCuotas=cantCuotas;
    }

    public Long getId(){
        return ahorroId;
    }

    public void setId(Long ahorroId){
        this.ahorroId= ahorroId;
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

    public BigDecimal getAcumulado(){
        return totalAcumulado;
    }
    
    public void setAcumulado(BigDecimal totalAcumulado){
        this.totalAcumulado=totalAcumulado;
    }

    public Frecuencia getFrecuencia(){
        return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia){
        this.frecuencia=frecuencia;
    }

    public LocalDate getCreacion(){
        return fechaCreacion;
    }

    public void setCreacion(LocalDate fechaCreacion){
        this.fechaCreacion=fechaCreacion;
    }

    public LocalDate getMeta(){
        return fechaMeta;
    }

    public void setMeta(LocalDate fechaMeta){
        this.fechaMeta=fechaMeta;
    }

    public Estado getEstado(){
        return estado;
    }

    public void setEstado(Estado estado){
        this.estado=estado;
    }

    public Integer getCantCuotas(){
        return cantidadCuotas;
    }

    public void setCantCuotas( Integer cantidadCuotas){
        this.cantidadCuotas= cantidadCuotas;
    }
    
}
