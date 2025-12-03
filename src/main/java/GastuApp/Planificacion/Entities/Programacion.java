package GastuApp.Planificacion.Entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "programacion")
public class Programacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "programacion_id")
    private Long id;

    @Column(name = "monto_programado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoProgramado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoConcepto tipo;   

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "frecuencia", length = 30)
    private String frecuencia;

    @Column(name = "activo", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Byte activo;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "concepto_id", nullable = false)
    private Long conceptoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    public enum TipoConcepto {
    INGRESO,
    EGRESO
}
}
