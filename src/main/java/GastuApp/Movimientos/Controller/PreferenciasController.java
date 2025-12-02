package GastuApp.Movimientos.Controller;

import GastuApp.Movimientos.DTO.PreferenciasFinancierasDTO;
import GastuApp.Movimientos.Service.PreferenciasUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de preferencias financieras del usuario.
 * Permite configurar umbrales de validación de salud financiera.
 */
@RestController
@RequestMapping("/api/movimientos/preferencias")
public class PreferenciasController {

    private final PreferenciasUsuarioService preferenciasService;

    public PreferenciasController(PreferenciasUsuarioService preferenciasService) {
        this.preferenciasService = preferenciasService;
    }

    /**
     * Obtiene las preferencias financieras del usuario autenticado.
     *
     * @param authentication Información del usuario autenticado
     * @return DTO con las preferencias actuales
     */
    @GetMapping
    public ResponseEntity<PreferenciasFinancierasDTO> obtenerPreferencias(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        PreferenciasFinancierasDTO preferencias = preferenciasService.obtenerPreferencias(usuarioId);
        return ResponseEntity.ok(preferencias);
    }

    /**
     * Actualiza las preferencias financieras del usuario autenticado.
     *
     * @param dto            DTO con las nuevas preferencias
     * @param authentication Información del usuario autenticado
     * @return DTO con las preferencias actualizadas
     */
    @PutMapping
    public ResponseEntity<PreferenciasFinancierasDTO> actualizarPreferencias(
            @Valid @RequestBody PreferenciasFinancierasDTO dto,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        PreferenciasFinancierasDTO preferenciasActualizadas = preferenciasService.actualizarPreferencias(usuarioId,
                dto);

        return ResponseEntity.ok(preferenciasActualizadas);
    }

    /**
     * Resetea las preferencias del usuario a los valores por defecto.
     *
     * @param authentication Información del usuario autenticado
     * @return DTO con las preferencias por defecto
     */
    @PostMapping("/reset")
    public ResponseEntity<PreferenciasFinancierasDTO> resetearPreferencias(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        PreferenciasFinancierasDTO preferencias = preferenciasService.resetearPreferencias(usuarioId);
        return ResponseEntity.ok(preferencias);
    }

    /**
     * Extrae el ID del usuario desde el objeto de autenticación.
     *
     * @param authentication Objeto de autenticación de Spring Security
     * @return ID del usuario autenticado
     */
    private Long obtenerUsuarioId(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("No hay información de autenticación");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof GastuApp.User.CustomUserDetails) {
            return ((GastuApp.User.CustomUserDetails) principal).getId();
        }

        // Fallback: si el principal es un String (username) o UserDetails estándar
        String username = null;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }

        if (username != null) {
            // Aquí idealmente inyectaríamos UsuarioRepository para buscar el ID,
            // pero por ahora lanzamos una excepción más descriptiva.
            // Si esto ocurre frecuentemente, inyectaremos el repositorio.
            System.err.println("Principal no es CustomUserDetails. Es: " + principal.getClass().getName()
                    + ", Username: " + username);
        } else {
            System.err
                    .println("Principal desconocido: " + (principal != null ? principal.getClass().getName() : "null"));
        }

        throw new RuntimeException("No se pudo obtener el ID del usuario. Tipo de principal: " +
                (principal != null ? principal.getClass().getName() : "null"));
    }
}
