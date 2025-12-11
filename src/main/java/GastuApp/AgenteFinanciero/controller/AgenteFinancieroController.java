package GastuApp.AgenteFinanciero.controller;

import GastuApp.AgenteFinanciero.dto.AgenteFinancieroDTO;
import GastuApp.AgenteFinanciero.dto.ConsultaRequest;
import GastuApp.AgenteFinanciero.entity.ConsultaIA;
import GastuApp.AgenteFinanciero.service.AgenteFinancieroService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agente-financiero")
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*")
@Slf4j
public class AgenteFinancieroController {
    
    @Autowired
    private AgenteFinancieroService agenteFinancieroService;
    
    @PostMapping("/consultar")
    public ResponseEntity<?> consultarAgente(@Valid @RequestBody ConsultaRequest request) {
        try {
            log.info("=== INICIO CONSULTA ===");
            log.info("Usuario ID: {}", request.getUsuarioId());
            log.info("Pregunta: {}", request.getPregunta());
            log.info("Incluir contexto: {}", request.getIncluirContextoFinanciero());
            
            AgenteFinancieroDTO respuesta = agenteFinancieroService.procesarConsulta(request);
            
            log.info("=== CONSULTA EXITOSA ===");
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("=== ERROR EN CONSULTA ===", e);
            log.error("Mensaje: {}", e.getMessage());
            log.error("Causa: {}", e.getCause() != null ? e.getCause().getMessage() : "N/A");
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error procesando la consulta");
            error.put("mensaje", e.getMessage());
            error.put("detalle", e.getCause() != null ? e.getCause().getMessage() : "");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/historial/{usuarioId}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable Long usuarioId) {
        try {
            log.info("Obteniendo historial para usuario: {}", usuarioId);
            List<ConsultaIA> historial = agenteFinancieroService.obtenerHistorialConsultas(usuarioId);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            log.error("Error obteniendo historial: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error obteniendo historial");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/consultas-del-dia/{usuarioId}")
    public ResponseEntity<?> consultasDelDia(@PathVariable Long usuarioId) {
        try {
            log.info("Contando consultas del día para usuario: {}", usuarioId);
            Long cantidad = agenteFinancieroService.contarConsultasDelDia(usuarioId);
            Map<String, Long> response = new HashMap<>();
            response.put("consultasHoy", cantidad);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error contando consultas: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error contando consultas");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Endpoint de prueba
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("mensaje", "API funcionando correctamente");
        return ResponseEntity.ok(response);
    }
}