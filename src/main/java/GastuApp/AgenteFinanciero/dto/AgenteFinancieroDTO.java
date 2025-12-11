package GastuApp.AgenteFinanciero.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenteFinancieroDTO {
    private String pregunta;
    private String respuesta;
    private LocalDateTime fechaConsulta;
    private Long usuarioId;
    private Boolean incluirDatosFinancieros;
}