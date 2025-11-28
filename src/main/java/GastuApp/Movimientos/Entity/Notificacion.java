package GastuApp.Movimientos.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación en el sistema.
 * Las notificaciones informan al usuario sobre eventos importantes
 * relacionados con sus movimientos financieros, programaciones, ahorros, etc.
 */
@Entity
@Table(name = "notificacion")
public class Notificacion {

    /**
     * Identificador único de la notificación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacion_id")
    private Long id;

    /**
     * ID del usuario destinatario de la notificación.
     * No se utiliza relación JPA para evitar dependencias entre microservicios.
     */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /**
     * Tipo de notificación.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoNotificacion tipo;

    /**
     * ID de referencia al objeto relacionado con la notificación.
     * Por ejemplo, si es una notificación de MOVIMIENTO, este campo
     * contiene el ID del movimiento que generó la notificación.
     */
    @Column(name = "referencia_id")
    private Long referenciaId;

    /**
     * Título de la notificación.
     */
    @Column(name = "titulo", length = 100)
    private String titulo;

    /**
     * Descripción detallada de la notificación.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Indica si la notificación ha sido leída por el usuario.
     * 0 = no leída, 1 = leída.
     */
    @Column(name = "leida", nullable = false)
    private Boolean leida = false;

    /**
     * Fecha y hora de creación de la notificación.
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Establece la fecha de creación automáticamente antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.leida == null) {
            this.leida = false;
        }
    }

    /**
     * Enumeración que define los tipos de notificación posibles.
     */
    public enum TipoNotificacion {
        PROGRAMACION,
        MOVIMIENTO,
        AHORRO,
        SISTEMA
    }

    // Constructores
    public Notificacion() {
    }

    public Notificacion(Long usuarioId, TipoNotificacion tipo, Long referenciaId,
            String titulo, String descripcion) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.leida = false;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
