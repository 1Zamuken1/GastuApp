package GastuApp.Movimientos.Entity;

import jakarta.persistence.*;

/**
 * Entidad que representa un concepto en el sistema.
 * Los conceptos clasifican los movimientos financieros (INGRESO, EGRESO,
 * AHORRO).
 * Esta tabla es global y compartida por todos los usuarios.
 */
@Entity
@Table(name = "concepto")
public class Concepto {

    /**
     * Identificador único del concepto.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concepto_id")
    private Long id;

    /**
     * Tipo de concepto: INGRESO, EGRESO o AHORRO.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoConcepto tipo;

    /**
     * Nombre del concepto.
     */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /**
     * Descripción del concepto.
     */
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    /**
     * Enumeración que define los tipos de concepto posibles.
     */
    public enum TipoConcepto {
        INGRESO,
        EGRESO,
        AHORRO
    }

    // Constructores
    public Concepto() {
    }

    public Concepto(TipoConcepto tipo, String nombre, String descripcion) {
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

    public TipoConcepto getTipo() {
        return tipo;
    }

    public void setTipo(TipoConcepto tipo) {
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
