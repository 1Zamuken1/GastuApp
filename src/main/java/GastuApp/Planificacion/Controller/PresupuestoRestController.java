package GastuApp.Planificacion.Controller;

import GastuApp.Planificacion.DTO.PresupuestoDTO;
import GastuApp.Planificacion.Service.PresupuestoService;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.User.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoRestController {

    private final PresupuestoService presupuestoService;
    private final ConceptoService conceptoService;

    @Autowired
    public PresupuestoRestController(PresupuestoService presupuestoService, ConceptoService conceptoService) {
        this.presupuestoService = presupuestoService;
        this.conceptoService = conceptoService;
    }

    // --------------------------
    // LISTAR TODOS LOS PRESUPUESTOS
    // --------------------------
    @GetMapping
    public ResponseEntity<List<PresupuestoDTO>> listarTodos(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.listarTodosPorUsuario(usuarioId));
    }

    // --------------------------
    // OBTENER PRESUPUESTO POR ID
    // --------------------------
    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoDTO> obtenerPorId(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.obtenerPorIdYUsuario(id, usuarioId));
    }

    // --------------------------
    // CREAR NUEVO PRESUPUESTO
    // --------------------------
    @PostMapping
    public ResponseEntity<PresupuestoDTO> crear(@Valid @RequestBody PresupuestoDTO dto, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.crear(dto, usuarioId));
    }

    // --------------------------
    // ACTUALIZAR PRESUPUESTO
    // --------------------------
    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoDTO> actualizar(@PathVariable Long id,
                                     @Valid @RequestBody PresupuestoDTO dto,
                                     Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.actualizar(id, dto, usuarioId));
    }

    // --------------------------
    // ELIMINAR PRESUPUESTO
    // --------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        presupuestoService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // --------------------------
    // LISTAR CONCEPTOS (para selects de los modales)
    // --------------------------
    @GetMapping("/conceptos")
    public ResponseEntity<?> listarConceptos() {
        return ResponseEntity.ok(conceptoService.obtenerTodos());
    }

    // --------------------------
    // Método helper para obtener usuario ID
    // --------------------------
    private Long obtenerUsuarioId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}