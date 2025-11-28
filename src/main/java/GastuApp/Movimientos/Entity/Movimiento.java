package GastuApp.Movimientos.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un movimiento financiero en el sistema.
 * Puede ser de tipo INGRESO o EGRESO.
 * Cada movimiento esta asociado a un usuario y opcionalmente a un concepto.
 */
@Entity
@Table(name = "movimiento")
public class Movimiento {

    /**
     * Identificador unico del movimiento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_id")
    private Long id;

    /**
     * Tipo de movimiento: INGRESO o EGRESO.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMovimiento tipo;

    /**
     * Monto del movimiento.
     * Debe ser mayor a cero.
     */
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /**
     * Descripcion opcional del movimiento.
     * Permite al usuario agregar notas sobre el movimiento.
     */
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    /**
     * Fecha y hora en que se registro el movimiento.
     * Se establece automaticamente al momento de la creacion.
     */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /**
     * ID del usuario propietario del movimiento.
     * No se utiliza relacion JPA para evitar dependencias entre microservicios.
     */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /**
     * ID del concepto asociado al movimiento.
     * Puede ser null si no se asigna concepto.
     * El concepto es gestionado por el microservicio de usuarios.
     */
    @Column(name = "concepto_id")
    private Long conceptoId;

    /**
     * Establece la fecha de registro automaticamente antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    /**
     * Enumeracion que define los tipos de movimiento posibles.
     */
    public enum TipoMovimiento {
        INGRESO,
        EGRESO
    }

    // Constructores
    public Movimiento() {
    }

    public Movimiento(Long id, TipoMovimiento tipo, BigDecimal monto, String descripcion, 
                      LocalDateTime fechaRegistro, Long usuarioId, Long conceptoId) {
        this.id = id;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
        this.usuarioId = usuarioId;
        this.conceptoId = conceptoId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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
}