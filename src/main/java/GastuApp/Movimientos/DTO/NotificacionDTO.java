package GastuApp.Movimientos.DTO;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Notificaciones.
 * Utilizado en las peticiones y respuestas del API REST.
 */
public class NotificacionDTO {

    /**
     * ID de la notificación. Solo se utiliza en respuestas.
     */
    private Long id;

    /**
     * Tipo de notificación.
     */
    private String tipo;

    /**
     * ID de referencia al objeto relacionado.
     */
    private Long referenciaId;

    /**
     * Título de la notificación.
     */
    private String titulo;

    /**
     * Descripción de la notificación.
     */
    private String descripcion;

    /**
     * Indica si la notificación ha sido leída.
     */
    private Boolean leida;

    /**
     * Fecha de creación de la notificación.
     */
    private LocalDateTime fechaCreacion;

    // Constructores
    public NotificacionDTO() {
    }

    public NotificacionDTO(Long id, String tipo, Long referenciaId, String titulo,
            String descripcion, Boolean leida, LocalDateTime fechaCreacion) {
        this.id = id;
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.leida = leida;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
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
