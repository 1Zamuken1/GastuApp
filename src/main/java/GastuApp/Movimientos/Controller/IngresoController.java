package GastuApp.Movimientos.Controller;

import GastuApp.Movimientos.DTO.IngresoDTO;
import GastuApp.Movimientos.Service.IngresoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestion de Ingresos.
 * Expone endpoints para operaciones CRUD sobre ingresos financieros.
 * Todos los endpoints requieren autenticacion JWT.
 */
@RestController
@RequestMapping("/api/movimientos/ingresos")
public class IngresoController {

    private final IngresoService ingresoService;

    public IngresoController(IngresoService ingresoService) {
        this.ingresoService = ingresoService;
    }

    /**
     * Endpoint de prueba sin autenticacion.
     * Solo para verificar que el microservicio esta funcionando.
     * ELIMINAR EN PRODUCCION.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Microservicio de Movimientos funcionando correctamente");
    }

    /**
     * Obtiene todos los ingresos del usuario autenticado.
     *
     * @param authentication Informacion del usuario autenticado (inyectada por
     *                       Spring Security)
     * @return Lista de ingresos del usuario ordenados por fecha descendente
     */
    @GetMapping
    public ResponseEntity<List<IngresoDTO>> listarIngresos(Authentication authentication) {
        System.out.println("DEBUG IngresoController: GET /api/movimientos/ingresos");
        Long usuarioId = obtenerUsuarioId(authentication);
        List<IngresoDTO> ingresos = ingresoService.obtenerIngresosPorUsuario(usuarioId);
        return ResponseEntity.ok(ingresos);
    }

    /**
     * Obtiene un ingreso especifico por su ID.
     * Solo permite acceder a ingresos propios del usuario autenticado.
     *
     * @param id             ID del ingreso a consultar
     * @param authentication Informacion del usuario autenticado
     * @return DTO del ingreso solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<IngresoDTO> obtenerIngresoPorId(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        IngresoDTO ingreso = ingresoService.obtenerIngresoPorId(id, usuarioId);
        return ResponseEntity.ok(ingreso);
    }

    /**
     * Crea un nuevo ingreso para el usuario autenticado.
     * Valida que todos los campos requeridos esten presentes y sean validos.
     *
     * @param dto            DTO con los datos del ingreso a crear
     * @param token          Token JWT para validar el concepto
     * @param authentication Informacion del usuario autenticado
     * @return DTO del ingreso creado con status 201 (CREATED)
     */
    @PostMapping
    public ResponseEntity<IngresoDTO> crearIngreso(
            @Valid @RequestBody IngresoDTO dto,
            @RequestHeader("Authorization") String token,
            Authentication authentication) {

        System.out.println("DEBUG IngresoController: POST /api/movimientos/ingresos - Petición recibida");
        System.out.println("DEBUG IngresoController: Authentication: "
                + (authentication != null ? authentication.getName() : "null"));

        Long usuarioId = obtenerUsuarioId(authentication);
        IngresoDTO ingresoCreado = ingresoService.crearIngreso(dto, usuarioId, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(ingresoCreado);
    }

    /**
     * Actualiza un ingreso existente.
     * Solo permite actualizar ingresos propios del usuario autenticado.
     *
     * @param id             ID del ingreso a actualizar
     * @param dto            DTO con los nuevos datos del ingreso
     * @param token          Token JWT para validar el concepto
     * @param authentication Informacion del usuario autenticado
     * @return DTO del ingreso actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<IngresoDTO> actualizarIngreso(
            @PathVariable Long id,
            @Valid @RequestBody IngresoDTO dto,
            @RequestHeader("Authorization") String token,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        IngresoDTO ingresoActualizado = ingresoService.actualizarIngreso(id, dto, usuarioId, token);
        return ResponseEntity.ok(ingresoActualizado);
    }

    /**
     * Elimina un ingreso.
     * Solo permite eliminar ingresos propios del usuario autenticado.
     *
     * @param id             ID del ingreso a eliminar
     * @param authentication Informacion del usuario autenticado
     * @return Respuesta sin contenido con status 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarIngreso(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        ingresoService.eliminarIngreso(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrae el ID del usuario desde el objeto de autenticacion.
     * Utiliza CustomUserDetails para obtener el ID real del usuario autenticado.
     *
     * @param authentication Objeto de autenticacion de Spring Security
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