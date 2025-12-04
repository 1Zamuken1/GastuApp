package GastuApp.Ahorro.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import GastuApp.Ahorro.Entity.AporteAhorro.EstadoAp;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class AporteAhorroDTO {

    private Long metaId;
    private Long aporteAhorroId;
    private BigDecimal aporteAsignado;

    @NotNull(message = "El aporte es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal aporte;
    
    private LocalDate fechaLimite;
    private EstadoAp estadoAp;
    private LocalDate fechaRegistro;

    public AporteAhorroDTO() {
    }

    public AporteAhorroDTO(Long metaId, Long aporteAhorroId, BigDecimal aporteAsignado, BigDecimal aporte, LocalDate fechaLimite, 
                        EstadoAp estadoAp, LocalDate fechaRegistro) {
        this.metaId = metaId;
        this.aporteAhorroId = aporteAhorroId;
        this.aporteAsignado = aporteAsignado;
        this.aporte=aporte;
        this.fechaLimite = fechaLimite;
        this.estadoAp = estadoAp;
        this.fechaRegistro=fechaRegistro;
        
    }

    public Long getMetaId() {
        return metaId;
    }

    public void setMetaId(Long metaId) {
        this.metaId = metaId;
    }

    public Long getAporteAhorroId() {
        return aporteAhorroId;
    }

    public void setAporteAhorroId(Long aporteAhorroId) {
        this.aporteAhorroId = aporteAhorroId;
    }

    public BigDecimal getAporteAsignado() {
        return aporteAsignado;
    }

    public void setAporteAsignado(BigDecimal aporteAsignado) {
        this.aporteAsignado = aporteAsignado;
    }

    public BigDecimal getAporte() {
        return aporte;
    }

    public void setAporte(BigDecimal aporte) {
        this.aporte = aporte;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public EstadoAp getEstado() {
        return estadoAp;
    }

    public void setEstado(EstadoAp estadoAp) {
        this.estadoAp = estadoAp;
    }

    // Fecha Registro Solo Getter, no debe ser modificable después de la creación
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    

}
