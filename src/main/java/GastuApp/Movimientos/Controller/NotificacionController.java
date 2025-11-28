package GastuApp.Movimientos.Controller;

import GastuApp.Movimientos.DTO.NotificacionDTO;
import GastuApp.Movimientos.Service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de notificaciones.
 * Permite consultar y actualizar notificaciones del usuario.
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Obtiene todas las notificaciones del usuario autenticado.
     *
     * @param authentication Información del usuario autenticado
     * @return Lista de notificaciones ordenadas por fecha descendente
     */
    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> listarNotificaciones(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        List<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesPorUsuario(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Obtiene las notificaciones no leídas del usuario autenticado.
     *
     * @param authentication Información del usuario autenticado
     * @return Lista de notificaciones no leídas
     */
    @GetMapping("/no-leidas")
    public ResponseEntity<List<NotificacionDTO>> listarNotificacionesNoLeidas(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        List<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesNoLeidas(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Cuenta las notificaciones no leídas del usuario autenticado.
     *
     * @param authentication Información del usuario autenticado
     * @return Objeto JSON con la cantidad de notificaciones no leídas
     */
    @GetMapping("/no-leidas/count")
    public ResponseEntity<Map<String, Long>> contarNotificacionesNoLeidas(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        Long count = notificacionService.contarNoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Marca una notificación como leída.
     *
     * @param id             ID de la notificación
     * @param authentication Información del usuario autenticado
     * @return DTO de la notificación actualizada
     */
    @PutMapping("/{id}/marcar-leida")
    public ResponseEntity<NotificacionDTO> marcarComoLeida(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);
        NotificacionDTO notificacion = notificacionService.marcarComoLeida(id, usuarioId);
        return ResponseEntity.ok(notificacion);
    }

    /**
     * Extrae el ID del usuario desde el objeto de autenticación.
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
