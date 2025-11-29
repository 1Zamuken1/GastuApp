package GastuApp.Movimientos.Controller;

import GastuApp.Movimientos.DTO.EgresoDTO;
import GastuApp.Movimientos.Service.EgresoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Egresos.
 * Expone endpoints para operaciones CRUD sobre egresos financieros.
 * Todos los endpoints requieren autenticación JWT.
 */
@RestController
@RequestMapping("/api/movimientos/egresos")
public class EgresoController {

    private final EgresoService egresoService;

    public EgresoController(EgresoService egresoService) {
        this.egresoService = egresoService;
    }

    /**
     * Obtiene todos los egresos del usuario autenticado.
     *
     * @param authentication Información del usuario autenticado (inyectada por
     *                       Spring Security)
     * @return Lista de egresos del usuario ordenados por fecha descendente
     */
    @GetMapping
    public ResponseEntity<List<EgresoDTO>> listarEgresos(Authentication authentication) {
        System.out.println("DEBUG EgresoController: GET /api/movimientos/egresos");
        Long usuarioId = obtenerUsuarioId(authentication);
        List<EgresoDTO> egresos = egresoService.obtenerEgresosPorUsuario(usuarioId);
        return ResponseEntity.ok(egresos);
    }

    /**
     * Obtiene un egreso específico por su ID.
     * Solo permite acceder a egresos propios del usuario autenticado.
     *
     * @param id             ID del egreso a consultar
     * @param authentication Información del usuario autenticado
     * @return DTO del egreso solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<EgresoDTO> obtenerEgresoPorId(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        EgresoDTO egreso = egresoService.obtenerEgresoPorId(id, usuarioId);
        return ResponseEntity.ok(egreso);
    }

    /**
     * Crea un nuevo egreso para el usuario autenticado.
     * Valida que todos los campos requeridos estén presentes y sean válidos.
     * Realiza validaciones de salud financiera y genera notificaciones si es
     * necesario.
     *
     * @param dto            DTO con los datos del egreso a crear
     * @param token          Token JWT para validar el concepto
     * @param authentication Información del usuario autenticado
     * @return DTO del egreso creado con status 201 (CREATED)
     */
    @PostMapping
    public ResponseEntity<EgresoDTO> crearEgreso(
            @Valid @RequestBody EgresoDTO dto,
            Authentication authentication) {

        System.out.println("DEBUG EgresoController: POST /api/movimientos/egresos - Petición recibida");
        System.out.println("DEBUG EgresoController: Authentication: "
                + (authentication != null ? authentication.getName() : "null"));

        Long usuarioId = obtenerUsuarioId(authentication);
        EgresoDTO egresoCreado = egresoService.crearEgreso(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(egresoCreado);
    }

    /**
     * Actualiza un egreso existente.
     * Solo permite actualizar egresos propios del usuario autenticado.
     *
     * @param id             ID del egreso a actualizar
     * @param dto            DTO con los nuevos datos del egreso
     * @param token          Token JWT para validar el concepto
     * @param authentication Información del usuario autenticado
     * @return DTO del egreso actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<EgresoDTO> actualizarEgreso(
            @PathVariable Long id,
            @Valid @RequestBody EgresoDTO dto,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        EgresoDTO egresoActualizado = egresoService.actualizarEgreso(id, dto, usuarioId);
        return ResponseEntity.ok(egresoActualizado);
    }

    /**
     * Elimina un egreso.
     * Solo permite eliminar egresos propios del usuario autenticado.
     *
     * @param id             ID del egreso a eliminar
     * @param authentication Información del usuario autenticado
     * @return Respuesta sin contenido con status 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEgreso(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        egresoService.eliminarEgreso(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene un resumen de conceptos con estadísticas (cantidad y total).
     * Solo incluye conceptos que tienen al menos un registro.
     *
     * @param authentication Información del usuario autenticado
     * @return Lista de ConceptoResumenDTO
     */
    @GetMapping("/resumen-conceptos")
    public ResponseEntity<List<GastuApp.Movimientos.DTO.ConceptoResumenDTO>> obtenerResumenConceptos(
            Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        List<GastuApp.Movimientos.DTO.ConceptoResumenDTO> resumen = egresoService.obtenerResumenConceptos(usuarioId);
        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtiene los egresos de un concepto específico.
     *
     * @param conceptoId     ID del concepto
     * @param authentication Información del usuario autenticado
     * @return Lista de egresos del concepto
     */
    @GetMapping("/concepto/{conceptoId}")
    public ResponseEntity<List<EgresoDTO>> obtenerEgresosPorConcepto(
            @PathVariable Long conceptoId,
            Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        List<EgresoDTO> egresos = egresoService.obtenerEgresosPorConcepto(usuarioId, conceptoId);
        return ResponseEntity.ok(egresos);
    }

    /**
     * Extrae el ID del usuario desde el objeto de autenticación.
     * Utiliza CustomUserDetails para obtener el ID real del usuario autenticado.
     *
     * @param authentication Objeto de autenticación de Spring Security
     * @return ID del usuario autenticado
     */
    private Long obtenerUsuarioId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof GastuApp.User.CustomUserDetails) {
            GastuApp.User.CustomUserDetails userDetails = (GastuApp.User.CustomUserDetails) authentication
                    .getPrincipal();
            return userDetails.getId();
        }
        throw new RuntimeException("No se pudo obtener el ID del usuario autenticado");
    }
}
