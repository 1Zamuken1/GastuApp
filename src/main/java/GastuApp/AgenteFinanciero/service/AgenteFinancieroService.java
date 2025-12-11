package GastuApp.AgenteFinanciero.service;

import GastuApp.AgenteFinanciero.dto.*;
import GastuApp.AgenteFinanciero.entity.ConsultaIA;
import GastuApp.AgenteFinanciero.repository.ConsultaIARepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AgenteFinancieroService {
    
    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;
    
    @Value("${groq.api.key}")
    private String groqApiKey;
    
    @Value("${groq.model:llama-3.1-70b-versatile}")
    private String modelo;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ConsultaIARepository consultaIARepository;
    
    @Autowired
    private ContextoFinancieroService contextoFinancieroService;
    
    private static final String SYSTEM_PROMPT = 
        "Eres un asistente financiero inteligente especializado en gestión de gastos personales. " +
        "Tu objetivo es ayudar a los usuarios a entender mejor sus finanzas, " +
        "ofrecer consejos personalizados basados en sus datos financieros, " +
        "y responder preguntas tanto generales sobre finanzas personales como específicas sobre sus gastos. " +
        "Sé claro, conciso y amigable. Usa números y datos cuando estén disponibles. " +
        "Si no tienes suficiente información para responder algo específico, indícalo claramente.";
    
    public AgenteFinancieroDTO procesarConsulta(ConsultaRequest request) {
        try {
            log.info("Procesando consulta para usuario: {}", request.getUsuarioId());
            
            // Construir el contexto
            String contextoCompleto = construirContexto(request);
            
            // Llamar a Groq API
            String respuesta = llamarGroqAPI(contextoCompleto);
            
            // Guardar consulta en BD
            ConsultaIA consultaIA = guardarConsulta(request, respuesta);
            
            // Retornar DTO
            return convertirADTO(consultaIA);
            
        } catch (Exception e) {
            log.error("Error procesando consulta: ", e);
            throw new RuntimeException("Error al procesar la consulta: " + e.getMessage());
        }
    }
    
    private String construirContexto(ConsultaRequest request) {
        StringBuilder contexto = new StringBuilder();
        
        // Agregar contexto financiero si se solicita
        if (Boolean.TRUE.equals(request.getIncluirContextoFinanciero())) {
            String datosFinancieros = contextoFinancieroService
                .obtenerContextoFinanciero(request.getUsuarioId());
            contexto.append(datosFinancieros).append("\n\n");
        }
        
        // Agregar historial reciente de consultas para contexto
        List<ConsultaIA> consultasRecientes = consultaIARepository
            .findConsultasRecientes(request.getUsuarioId(), LocalDateTime.now().minusHours(2));
        
        if (!consultasRecientes.isEmpty()) {
            contexto.append("HISTORIAL RECIENTE DE CONVERSACIÓN:\n");
            for (ConsultaIA consulta : consultasRecientes) {
                contexto.append("Usuario: ").append(consulta.getPregunta()).append("\n");
                contexto.append("Asistente: ").append(consulta.getRespuesta()).append("\n\n");
            }
        }
        
        contexto.append("PREGUNTA ACTUAL DEL USUARIO:\n");
        contexto.append(request.getPregunta());
        
        return contexto.toString();
    }
    
    private String llamarGroqAPI(String contexto) {
        try {
            // Construir mensajes
            List<GroqRequest.Message> mensajes = new ArrayList<>();
            mensajes.add(GroqRequest.Message.builder()
                .role("system")
                .content(SYSTEM_PROMPT)
                .build());
            
            mensajes.add(GroqRequest.Message.builder()
                .role("user")
                .content(contexto)
                .build());
            
            // Construir request
            GroqRequest groqRequest = GroqRequest.builder()
                .messages(mensajes)
                .model(modelo)
                .temperature(0.7)
                .max_tokens(1000)
                .stream(false)
                .build();
            
            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);
            
            HttpEntity<GroqRequest> entity = new HttpEntity<>(groqRequest, headers);
            
            // Llamar API
            ResponseEntity<GroqResponse> response = restTemplate.exchange(
                groqApiUrl,
                HttpMethod.POST,
                entity,
                GroqResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GroqResponse groqResponse = response.getBody();
                if (groqResponse.getChoices() != null && !groqResponse.getChoices().isEmpty()) {
                    return groqResponse.getChoices().get(0).getMessage().getContent();
                }
            }
            
            throw new RuntimeException("No se recibió respuesta válida de Groq API");
            
        } catch (Exception e) {
            log.error("Error llamando a Groq API: ", e);
            throw new RuntimeException("Error comunicándose con la IA: " + e.getMessage());
        }
    }
    
    private ConsultaIA guardarConsulta(ConsultaRequest request, String respuesta) {
        ConsultaIA consultaIA = new ConsultaIA();
        consultaIA.setUsuarioId(request.getUsuarioId());
        consultaIA.setPregunta(request.getPregunta());
        consultaIA.setRespuesta(respuesta);
        consultaIA.setIncluyoDatosFinancieros(request.getIncluirContextoFinanciero());
        consultaIA.setModeloUsado(modelo);
        
        return consultaIARepository.save(consultaIA);
    }
    
    private AgenteFinancieroDTO convertirADTO(ConsultaIA consultaIA) {
        AgenteFinancieroDTO dto = new AgenteFinancieroDTO();
        dto.setPregunta(consultaIA.getPregunta());
        dto.setRespuesta(consultaIA.getRespuesta());
        dto.setFechaConsulta(consultaIA.getFechaConsulta());
        dto.setUsuarioId(consultaIA.getUsuarioId());
        dto.setIncluirDatosFinancieros(consultaIA.getIncluyoDatosFinancieros());
        return dto;
    }
    
    public List<ConsultaIA> obtenerHistorialConsultas(Long usuarioId) {
        return consultaIARepository.findByUsuarioIdOrderByFechaConsultaDesc(usuarioId);
    }
    
    public Long contarConsultasDelDia(Long usuarioId) {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return consultaIARepository.contarConsultasDelDia(usuarioId, inicioDia);
    }
}