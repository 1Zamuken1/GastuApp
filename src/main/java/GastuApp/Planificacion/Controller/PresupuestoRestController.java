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

    // ================================================================
    // LISTAR TODOS (ESTO ES SIN PROGRESO, SOLO INFO DEL PRESUPUESTO)
    // ================================================================
    @GetMapping
    public ResponseEntity<List<PresupuestoDTO>> listarTodos(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.listarTodosPorUsuario(usuarioId));
    }

    // ================================================================
    // 🚀 NUEVO: LISTAR CON PROGRESO (ESTO ES LO QUE USA LA BARRA)
    // ================================================================
    @GetMapping("/progreso")
    public ResponseEntity<List<PresupuestoDTO>> obtenerProgreso(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.obtenerPresupuestosConProgreso(usuarioId));
    }

    // ================================================================
    // OBTENER POR ID
    // ================================================================
    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoDTO> obtenerPorId(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.obtenerPorIdYUsuario(id, usuarioId));
    }

    // ================================================================
    // CREAR
    // ================================================================
    @PostMapping
    public ResponseEntity<PresupuestoDTO> crear(@Valid @RequestBody PresupuestoDTO dto,
                                                Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.crear(dto, usuarioId));
    }

    // ================================================================
    // ACTUALIZAR
    // ================================================================
    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoDTO> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody PresupuestoDTO dto,
                                                     Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.actualizar(id, dto, usuarioId));
    }

    // ================================================================
    // ELIMINAR
    // ================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        presupuestoService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // LISTAR CONCEPTOS DE EGRESOS
    // ================================================================
    @GetMapping("/conceptos")
    public ResponseEntity<?> listarConceptos() {
        return ResponseEntity.ok(conceptoService.obtenerSoloEgresos());
    }

    // ================================================================
    // OBTENER ID DEL USUARIO LOGUEADO
    // ================================================================
    private Long obtenerUsuarioId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
    @PatchMapping("/activar/{id}")
    public ResponseEntity<PresupuestoDTO> activar(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        return ResponseEntity.ok(presupuestoService.activarPresupuesto(id, usuarioId));
    }

}
