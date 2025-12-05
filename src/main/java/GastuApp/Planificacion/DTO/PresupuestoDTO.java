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
    @Digits(integer = 12, fraction = 2, message = "El valor límite no puede tener más de 12 dígitos enteros y 2 decimales")
    private BigDecimal limite;

    @NotNull(message = "La fecha inicio es obligatoria")
    @PastOrPresent(message = "La fecha inicio no puede ser futura")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha final es obligatoria")
    @FutureOrPresent(message = "La fecha final no puede ser pasada")
    private LocalDate fechaFin;

    private Boolean activo = true;

    private Double gastado;  // <- SE LLENA EN EL SERVICIO

    private Integer porcentaje; // <- AGREGADO

    private LocalDateTime fechaCreacion;

    @NotNull(message = "El concepto es requerido")
    private Long conceptoId;

    private String conceptoNombre;
}
