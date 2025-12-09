package GastuApp.Planificacion.DTO;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramacionDTO {

    private Long id;

    private BigDecimal montoProgramado;

    private String tipo; // INGRESO / EGRESO

    private String descripcion;

    private LocalDate fechaInicio;

    private LocalDate proximaEjecucion;

    private String frecuencia;

    private Long conceptoId;

    private String conceptoNombre;

    private boolean activo;
}
