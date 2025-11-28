package GastuApp.Movimientos.DTO;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Ingresos.
 * Utilizado en las peticiones y respuestas del API REST.
 */
public class IngresoDTO {

    /**
     * ID del ingreso. Solo se utiliza en respuestas.
     */
    private Long id;

    /**
     * Monto del ingreso.
     * Debe ser mayor a cero.
     */
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener maximo 10 digitos enteros y 2 decimales")
    private BigDecimal monto;

    /**
     * Descripcion del ingreso.
     * Campo opcional, maximo 100 caracteres.
     */
    @Size(max = 100, message = "La descripcion no puede exceder 100 caracteres")
    private String descripcion;

    /**
     * ID del concepto de ingreso seleccionado.
     * Debe existir en la tabla concepto con tipo INGRESO.
     */
    @NotNull(message = "El concepto es obligatorio")
    private Long conceptoId;

    /**
     * Fecha de registro del ingreso.
     * Se establece automaticamente en el servidor.
     */
    private LocalDateTime fechaRegistro;

    // Constructores
    public IngresoDTO() {
    }

    public IngresoDTO(Long id, BigDecimal monto, String descripcion, Long conceptoId, LocalDateTime fechaRegistro) {
        this.id = id;
        this.monto = monto;
        this.descripcion = descripcion;
        this.conceptoId = conceptoId;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getConceptoId() {
        return conceptoId;
    }

    public void setConceptoId(Long conceptoId) {
        this.conceptoId = conceptoId;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}