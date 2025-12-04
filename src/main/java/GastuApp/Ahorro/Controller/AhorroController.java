package GastuApp.Ahorro.Controller;


import GastuApp.Ahorro.DTO.AhorroDTO;
import GastuApp.Ahorro.DTO.AporteAhorroDTO;
import GastuApp.Ahorro.DTO.CrearAhorroDTO;
import GastuApp.Ahorro.DTO.EditarAhorroDTO;
import GastuApp.Ahorro.Entity.AhorroMeta;
import GastuApp.Ahorro.Service.AhorroService;
import GastuApp.User.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ahorros")
public class AhorroController {

    private final AhorroService ahorroService;

    @Autowired
    public AhorroController(AhorroService ahorroService) {
        this.ahorroService = ahorroService;
    }

    // OBTENER usuarioId AUTENTICADO
    private Long obtenerUsuarioId(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getId();
    }

    // LISTAR TODOS LOS AHORROS DEL USUARIO
    @GetMapping
    public ResponseEntity<List<AhorroDTO>> listar(
            @RequestParam(required = false) String estado,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        if (estado != null && !estado.isEmpty()) {
            try {
            //  Convertir estado de string a enum
            AhorroMeta.Estado estadoEnum = AhorroMeta.Estado.valueOf(estado.toUpperCase());
            
            return ResponseEntity.ok(
                    ahorroService.filtrarPorEstado(usuarioId, estadoEnum) // <-- Pasa el Enum
            );
        } catch (IllegalArgumentException e) {
            // Manejar si el estado proporcionado no es válido
            List<AhorroDTO> listaVaciaTipada = new ArrayList<>();
            return ResponseEntity.badRequest().body(listaVaciaTipada);
        }
    }
    return ResponseEntity.ok(
        ahorroService.listarTodosPorUsuario(usuarioId)
        );
    }

    // OBTENER AHORRO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<AhorroDTO> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return ResponseEntity.ok(
                ahorroService.obtenerPorIdYUsuario(id, usuarioId)
        );
    }

    //  CREAR NUEVO AHORRO
    @PostMapping
    public ResponseEntity<AhorroDTO> crear(
            @Valid @RequestBody CrearAhorroDTO dto,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return ResponseEntity.ok(
                ahorroService.crear(dto, usuarioId)
        );
    }

    // ACTUALIZAR AHORRO
    @PutMapping("/{id}")
    public ResponseEntity<AhorroDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EditarAhorroDTO dto,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return ResponseEntity.ok(
                ahorroService.actualizar(id, dto, usuarioId)
        );
    }

    //  ELIMINAR AHORRO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        ahorroService.eliminar(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    // 6. LISTAR TODAS LAS CUOTAS DE UN AHORRO
    @GetMapping("/cuotas/{id}")
    public ResponseEntity<List<AporteAhorroDTO>> listarCuotas(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return ResponseEntity.ok(
                ahorroService.listarAportesPorMeta(id, usuarioId)
        );
    }

    //  OBTENER LA PRÓXIMA CUOTA DISPONIBLE
    @GetMapping("/cuotas/proxima/{id}")
    public ResponseEntity<AporteAhorroDTO> obtenerProximaCuota(
            @PathVariable Long id,
            Authentication authentication) {
            
            Long usuarioId= obtenerUsuarioId(authentication);
            java.util.Optional<GastuApp.Ahorro.Entity.AporteAhorro> cuotaOptional = 
            ahorroService.obtenerCuotaDisponible(id, usuarioId);
            
        return cuotaOptional
        .map(ahorroService::toAporteDTO)
        .map(ResponseEntity::ok) // Si hay DTO, devuelve 200 OK
        .orElseGet(() -> ResponseEntity.notFound().build());

    }

    //  REGISTRAR APORTE
    @PostMapping("/{id}/cuotas/{cuotaId}")
    public ResponseEntity<AporteAhorroDTO> registrarAporte(
            @PathVariable Long id,
            @PathVariable Long cuotaId,
            @Valid @RequestBody AporteAhorroDTO dto,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return ResponseEntity.ok(
                ahorroService.registrarAporte(id, cuotaId, dto, usuarioId)
        );
    }
}
