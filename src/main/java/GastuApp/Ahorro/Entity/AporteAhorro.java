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

    @Column(name = "meta_id", nullable = false)
    private Long metaId;

    @Column(name="aporteAsignado")
    private BigDecimal aporteAsignado;

    @Column(name="aporte")
    private BigDecimal aporte;

    @Column(name="fechaLimite", nullable = false)
    private LocalDate fechaLimite;

    public enum EstadoAp{
        APORTADO,
        PERDIDO,
        PENDIENTE
    }
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAp estadoAp;

    @Column(name = "fechaRegistro", nullable = false, updatable = false)
    private LocalDate fechaRegistro;
    

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDate.now();
    }

    public AporteAhorro() {
    }

    public AporteAhorro(Long aporteAhorroId, Long metaId, BigDecimal aporteAsignado, BigDecimal aporte, LocalDate fechaLimite, 
                        EstadoAp estadoAp, LocalDate fechaRegistro) {
        this.aporteAhorroId = aporteAhorroId;
        this.metaId = metaId;
        this.aporteAsignado = aporteAsignado;
        this.aporte=aporte;
        this.fechaLimite = fechaLimite;
        this.estadoAp = estadoAp;
        this.fechaRegistro=fechaRegistro;
    }

    public Long getAporteAhorroId() {
        return aporteAhorroId;
    }

    public void setAporteAhorroId(Long aporteAhorroId) {
        this.aporteAhorroId = aporteAhorroId;
    }

    public Long getMetaId() {
        return metaId;
    }

    public void setMetaId(Long metaId) {
        this.metaId = metaId;
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
    

// tiene una relacion uno a muchos con ahorro meta
}