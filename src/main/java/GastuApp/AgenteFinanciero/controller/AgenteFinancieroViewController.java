package GastuApp.AgenteFinanciero.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

import GastuApp.User.Usuario;
import GastuApp.User.UsuarioService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class AgenteFinancieroViewController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/agente-financiero")
    public String vistaAgenteFinanciero(Authentication authentication, Model model) {
        
        // Validar autenticación
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Usuario no autenticado intentando acceder a agente financiero");
            return "redirect:/login";
        }

        try {
            // Obtener el identificador del usuario autenticado (puede ser username o email)
            String loginIdentifier = authentication.getName();
            log.info("Usuario autenticado accediendo a agente financiero: {}", loginIdentifier);

            // Buscar usuario por username o email
            Usuario usuario = usuarioService.buscarPorUsernameOEmail(loginIdentifier);
            
            log.info("Usuario encontrado - ID: {}, Username: {}, Email: {}", 
                usuario.getId(), usuario.getUsername(), usuario.getEmail());

            // Pasar el ID al modelo
            model.addAttribute("usuarioId", usuario.getId());
            model.addAttribute("activePage", "agenteFinanciero");

            return "agenteFinanciero/agente-financiero";
            
        } catch (Exception e) {
            log.error("Error obteniendo usuario autenticado: ", e);
            return "redirect:/login?error=user_not_found";
        }
    }

    @GetMapping("/agente-financiero/user-id")
    @ResponseBody
    public Map<String, Object> obtenerUserId(Authentication authentication) {
        
        // Validar autenticación
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Intento de acceso no autenticado a /agente-financiero/user-id");
            throw new RuntimeException("No autenticado");
        }

        try {
            String loginIdentifier = authentication.getName();
            log.info("Solicitando userId para: {}", loginIdentifier);

            // Buscar usuario
            Usuario usuario = usuarioService.buscarPorUsernameOEmail(loginIdentifier);
            
            log.info("UserId encontrado: {}", usuario.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("username", usuario.getUsername());
            response.put("email", usuario.getEmail());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error obteniendo userId: ", e);
            throw new RuntimeException("Error obteniendo información del usuario");
        }
    }
}