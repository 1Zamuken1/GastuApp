package GastuApp.Config;

import GastuApp.User.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    /**
     * Retorna el id del usuario autenticado.
     * Lanza RuntimeException si no hay autenticación o si el principal no es CustomUserDetails.
     */
    public static Long obtenerUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("Usuario no autenticado (Authentication = null)");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        } else if (principal instanceof String) {
            // En algunos casos (por ejemplo cuando no hay auth fully populated) principal puede ser username
            throw new RuntimeException("Principal es String (username). No se pudo obtener id. Asegure que CustomUserDetails esté cargado.");
        } else {
            throw new RuntimeException("Principal inesperado: " + principal.getClass().getName());
        }
    }
}
