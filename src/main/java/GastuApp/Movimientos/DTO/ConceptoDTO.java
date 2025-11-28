package GastuApp.Movimientos.DTO;

/**
 * DTO para transferencia de datos de Conceptos.
 * Representa la informacion basica de un concepto que proviene
 * del microservicio de gestion de usuarios.
 */
public class ConceptoDTO {

    /**
     * ID del concepto.
     */
    private Long conceptoId;

    /**
     * Tipo de concepto: INGRESO, EGRESO o AHORRO.
     */
    private String tipo;

    /**
     * Nombre del concepto.
     */
    private String nombre;

    /**
     * Descripcion del concepto.
     */
    private String descripcion;

    // Constructores
    public ConceptoDTO() {
    }

    public ConceptoDTO(Long conceptoId, String tipo, String nombre, String descripcion) {
        this.conceptoId = conceptoId;
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Long getConceptoId() {
        return conceptoId;
    }

    public void setConceptoId(Long conceptoId) {
        this.conceptoId = conceptoId;
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