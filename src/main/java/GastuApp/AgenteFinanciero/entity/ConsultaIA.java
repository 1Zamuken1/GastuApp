package GastuApp.AgenteFinanciero.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consulta_ia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaIA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consulta_id")
    private Long consultaId;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(name = "pregunta", columnDefinition = "TEXT", nullable = false)
    private String pregunta;
    
    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;
    
    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;
    
    @Column(name = "incluyo_datos_financieros")
    private Boolean incluyoDatosFinancieros;
    
    @Column(name = "tokens_usados")
    private Integer tokensUsados;
    
    @Column(name = "modelo_usado")
    private String modeloUsado;
    
    @PrePersist
    protected void onCreate() {
        fechaConsulta = LocalDateTime.now();
    }
}