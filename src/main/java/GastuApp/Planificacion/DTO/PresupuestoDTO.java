package GastuApp.Planificacion.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PresupuestoDTO {

    private Long id;

    @NotNull(message = "El valor límite es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor límite debe ser mayor a 0")
    private BigDecimal limite;
    
    @NotNull(message = "La fecha inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "la fecha final es obligatoria")
    private LocalDate fechaFin;

    private Boolean activo = true;

    private LocalDateTime fechaCreacion;

    @NotNull(message = "El concepto es requerido")
    private Long conceptoId;

    private String conceptoNombre;
}