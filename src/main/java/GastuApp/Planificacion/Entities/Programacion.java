package GastuApp.Planificacion.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "programacion")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Programacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "programacion_id")
    private Long id;

    @Column(name = "monto_programado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoProgramado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProgramacion tipo; // INGRESO O EGRESO

    @Column(length = 100)
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "proxima_ejecucion")
    private LocalDate proximaEjecucion;

    @Column(name = "frecuencia", length = 30, nullable = false)
    private String frecuencia;

    @Column(name = "activo", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean activo = true;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "concepto_id", nullable = false)
    private Long conceptoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public enum TipoProgramacion {
        INGRESO,
        EGRESO
    }
}
