package GastuApp.AgenteFinanciero.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaRequest {
    
    @NotBlank(message = "La pregunta no puede estar vacía")
    private String pregunta;
    
    @NotNull(message = "El ID de usuario es requerido")
    private Long usuarioId;
    
    private Boolean incluirContextoFinanciero = true;
}