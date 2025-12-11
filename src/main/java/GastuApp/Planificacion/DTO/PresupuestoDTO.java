package GastuApp.Planificacion.DTO;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PresupuestoDTO {

    private Long id;

    @NotNull(message = "El valor límite es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor límite debe ser mayor a 0")
    @Digits(integer = 12, fraction = 2, message = "El valor límite no puede tener más de 12 dígitos enteros y 2 decimales")
    private BigDecimal limite;

    @NotNull(message = "La fecha inicio es obligatoria")
    @PastOrPresent(message = "La fecha inicio no puede ser futura")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha final es obligatoria")
    @FutureOrPresent(message = "La fecha final no puede ser pasada")
    private LocalDate fechaFin;

    private Boolean activo = true;

    private Double gastado; // <- SE LLENA EN EL SERVICIO

    private Integer porcentaje; // <- AGREGADO

    private LocalDateTime fechaCreacion;

    @NotNull(message = "El concepto es requerido")
    private Long conceptoId;

    private String conceptoNombre;

    public PresupuestoDTO() {
    }

    public PresupuestoDTO(Long id, BigDecimal limite, LocalDate fechaInicio, LocalDate fechaFin, Boolean activo,
            Double gastado, Integer porcentaje, LocalDateTime fechaCreacion, Long conceptoId, String conceptoNombre) {
        this.id = id;
        this.limite = limite;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = activo;
        this.gastado = gastado;
        this.porcentaje = porcentaje;
        this.fechaCreacion = fechaCreacion;
        this.conceptoId = conceptoId;
        this.conceptoNombre = conceptoNombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getLimite() {
        return limite;
    }

    public void setLimite(BigDecimal limite) {
        this.limite = limite;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Double getGastado() {
        return gastado;
    }

    public void setGastado(Double gastado) {
        this.gastado = gastado;
    }

    public Integer getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje) {
        this.porcentaje = porcentaje;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getConceptoId() {
        return conceptoId;
    }

    public void setConceptoId(Long conceptoId) {
        this.conceptoId = conceptoId;
    }

    public String getConceptoNombre() {
        return conceptoNombre;
    }

    public void setConceptoNombre(String conceptoNombre) {
        this.conceptoNombre = conceptoNombre;
    }
}
