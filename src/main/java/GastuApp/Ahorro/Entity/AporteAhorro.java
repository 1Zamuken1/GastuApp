package GastuApp.Ahorro.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name= "aporteAhorro")
public class AporteAhorro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aporteAhorroId;

    @Column(name="aporteAsignado")
    private BigDecimal aporteAsignado;

    @Column(name="aporte")
    private BigDecimal aporte;

    @Column(name="fechaLimite", nullable = false)
    private LocalDate fechaLimite;

    public enum Estado{
        APORTADO,
        PERDIDO,
        PENDIENTE
    }
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "fechaRegistro", nullable = false, updatable = false)
    private LocalDate fechaRegistro;
    
    @Column(name = "meta_id", nullable = false)
    private Long metaId;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDate.now();
    }

    public AporteAhorro() {
    }

    public AporteAhorro(Long aporteAhorroId, BigDecimal aporteAsignado, BigDecimal aporte, LocalDate fechaLimite, 
                        Estado estado, LocalDate fechaRegistro, Long metaId) {
        this.aporteAhorroId = aporteAhorroId;
        this.aporteAsignado = aporteAsignado;
        this.aporte=aporte;
        this.fechaLimite = fechaLimite;
        this.estado = estado;
        this.fechaRegistro=fechaRegistro;
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

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    // Fecha Registro Solo Getter, no debe ser modificable después de la creación
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    
    public Long getMetaId() {
        return metaId;
    }

    public void setMetaId(Long metaId) {
        this.metaId = metaId;
    }

// tiene una relacion uno a muchos con ahorro meta
}