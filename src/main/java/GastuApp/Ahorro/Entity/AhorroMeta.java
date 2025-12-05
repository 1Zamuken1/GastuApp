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
@Table(name = "ahorro_meta")
public class AhorroMeta {

    @Id
    @Column(name = "ahorro_meta_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ahorroId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "concepto_id", nullable = false)
    private Long conceptoId;

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Column(name = "monto_meta", nullable = false)
    private BigDecimal montoMeta;

    @Column(name = "total_acumulado", nullable = false)
    private BigDecimal totalAcumulado;

    public enum Frecuencia {
        DIARIA,
        SEMANAL,
        QUINCENAL,
        MENSUAL,
        TRIMESTRAL,
        SEMESTRAL,
        ANUAL
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "frecuencia", nullable = false)
    private Frecuencia frecuencia;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_meta", nullable = false)
    private LocalDate fechaMeta;

    public enum Estado {
        SININICIAR,
        ACTIVO,
        COMPLETADO,
        ABANDONADO
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "cantidad_cuotas", nullable = false)
    private Integer cantidadCuotas;

    /**
     * Establece la fecha de creacion automaticamente antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDate.now();
    }

    // contructor
    public AhorroMeta() {

    }

    public AhorroMeta(Long id, Long usuarioId, Long conceptoId, String descripcion, BigDecimal montoMeta,
            BigDecimal totalAcumulado, Frecuencia frecuencia, LocalDate fechaCreacion, LocalDate fechaMeta,
            Estado estado, Integer cantidadCuotas) {

        this.ahorroId = id;
        this.usuarioId = usuarioId;
        this.conceptoId = conceptoId;
        this.descripcion = descripcion;
        this.montoMeta = montoMeta;
        this.totalAcumulado = totalAcumulado;
        this.frecuencia = frecuencia;
        this.fechaCreacion = fechaCreacion;
        this.fechaMeta = fechaMeta;
        this.estado = estado;
        this.cantidadCuotas = cantidadCuotas;

    }
    // getter-> controla la salida del dato
    // setters-> controla la entrada del dato

    public Long getId() {
        return ahorroId;
    }

    public void setId(Long ahorroId) {
        this.ahorroId = ahorroId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getConceptoId() {
        return conceptoId;
    }

    public void setConceptoId(Long conceptoId) {
        this.conceptoId = conceptoId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMonto() {
        return montoMeta;
    }

    public void setMonto(BigDecimal montoMeta) {
        this.montoMeta = montoMeta;
    }

    public BigDecimal getAcumulado() {
        return totalAcumulado;
    }

    public void setAcumulado(BigDecimal totalAcumulado) {
        this.totalAcumulado = totalAcumulado;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia) {
        this.frecuencia = frecuencia;
    }

    public LocalDate getCreacion() {
        return fechaCreacion;
    }

    public void setCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getMeta() {
        return fechaMeta;
    }

    public void setMeta(LocalDate fechaMeta) {
        this.fechaMeta = fechaMeta;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Integer getCantCuotas() {
        return cantidadCuotas;
    }

    public void setCantCuotas(Integer cantidadCuotas) {
        this.cantidadCuotas = cantidadCuotas;
    }

    // TIENE RELACION UNO A MUCHOS CON USUARIOS Y CON CONCEPTOS
}
