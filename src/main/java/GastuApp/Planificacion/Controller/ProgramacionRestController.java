package GastuApp.Planificacion.Controller;

import GastuApp.Planificacion.DTO.ProgramacionDTO;
import GastuApp.Planificacion.Service.ProgramacionService;
import GastuApp.User.CustomUserDetails;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/programaciones")
public class ProgramacionRestController {

    private final ProgramacionService programacionService;

    public ProgramacionRestController(ProgramacionService programacionService) {
        this.programacionService = programacionService;
    }

    // Obtener ID del usuario autenticado
    private Long getUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    // ----------------------------------------------------
    // LISTAR PROGRAMACIONES DEL USUARIO AUTENTICADO
    // ----------------------------------------------------
    @GetMapping
    public ResponseEntity<List<ProgramacionDTO>> listar(Authentication auth) {
        Long usuarioId = getUserId(auth);
        return ResponseEntity.ok(programacionService.obtenerPorUsuario(usuarioId));
    }

    // ----------------------------------------------------
    // BUSQUEDA GLOBAL
    // ----------------------------------------------------
    @GetMapping("/buscar")
    public ResponseEntity<List<ProgramacionDTO>> buscar(
            @RequestParam String termino,
            Authentication auth) {

        Long usuarioId = getUserId(auth);
        return ResponseEntity.ok(programacionService.buscar(usuarioId, termino));
    }

    // ----------------------------------------------------
    // CREAR
    // ----------------------------------------------------
    @PostMapping
    public ResponseEntity<ProgramacionDTO> crear(
            @RequestBody ProgramacionDTO dto,
            Authentication auth) {

        Long usuarioId = getUserId(auth);
        return ResponseEntity.ok(programacionService.crear(dto, usuarioId));
    }

    // ----------------------------------------------------
    // EDITAR
    // ----------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ProgramacionDTO> editar(
            @PathVariable Long id,
            @RequestBody ProgramacionDTO dto,
            Authentication auth) {

        Long usuarioId = getUserId(auth);
        return ResponseEntity.ok(programacionService.editar(id, dto, usuarioId));
    }

    // ----------------------------------------------------
    // ACTIVAR / DESACTIVAR
    // ----------------------------------------------------
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            Authentication auth) {

        Long usuarioId = getUserId(auth);
        programacionService.cambiarEstado(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------
    // ELIMINAR
    // ----------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication auth) {

        Long usuarioId = getUserId(auth);
        programacionService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------
    // ACEPTAR EJECUCIÓN: genera movimiento (ingreso/egreso)
    // ----------------------------------------------------
    @PostMapping("/{id}/aceptar")
    public ResponseEntity<Void> aceptarEjecucion(
            @PathVariable Long id,
            Authentication auth) {

        Long usuarioId = getUserId(auth);
        programacionService.aceptar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------
    // RECHAZAR EJECUCIÓN (solo recalcula la próxima fecha)
    // ----------------------------------------------------
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazarEjecucion(
            @PathVariable Long id,
            Authentication auth) {

        Long usuarioId = getUserId(auth);
        programacionService.rechazar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
