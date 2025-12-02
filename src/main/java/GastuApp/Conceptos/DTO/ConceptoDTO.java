package GastuApp.Conceptos.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferencia de datos de Conceptos.
 * Utilizado en las peticiones y respuestas del API REST.
 */
public class ConceptoDTO {

    /**
     * ID del concepto. Solo se utiliza en respuestas.
     */
    private Long id;

    /**
     * Tipo de concepto: INGRESO, EGRESO o AHORRO.
     */
    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    /**
     * Nombre del concepto.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    /**
     * Descripción del concepto.
     */
    @Size(max = 100, message = "La descripción no puede exceder 100 caracteres")
    private String descripcion;

    // Constructores
    public ConceptoDTO() {
    }

    public ConceptoDTO(Long id, String tipo, String nombre, String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}