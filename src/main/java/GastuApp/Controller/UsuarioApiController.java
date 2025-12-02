package GastuApp.Controller;

import GastuApp.User.CustomUserDetails;
import GastuApp.User.Usuario;
import GastuApp.User.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioApiController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioApiController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get current user's profile information
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerPerfil(Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, Object> perfil = new HashMap<>();
        perfil.put("id", usuario.getId());
        perfil.put("username", usuario.getUsername());
        perfil.put("email", usuario.getEmail());
        perfil.put("telefono", usuario.getTelefono());

        return ResponseEntity.ok(perfil);
    }

    /**
     * Update user profile (username and email)
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> actualizarPerfil(
            @RequestBody Map<String, String> datos,
            Authentication authentication) {
        
        Long usuarioId = obtenerUsuarioId(authentication);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Update username if provided and different
        if (datos.containsKey("username") && !datos.get("username").equals(usuario.getUsername())) {
            String nuevoUsername = datos.get("username");
            if (usuarioRepository.existsByUsername(nuevoUsername)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
            }
            usuario.setUsername(nuevoUsername);
        }

        // Email is readonly for now, but we could add validation if needed
        
        usuarioRepository.save(usuario);

        Map<String, Object> perfil = new HashMap<>();
        perfil.put("id", usuario.getId());
        perfil.put("username", usuario.getUsername());
        perfil.put("email", usuario.getEmail());
        perfil.put("telefono", usuario.getTelefono());

        return ResponseEntity.ok(perfil);
    }

    /**
     * Change user password
     */
    @PostMapping("/cambiar-password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @RequestBody Map<String, String> datos,
            Authentication authentication) {
        
        Long usuarioId = obtenerUsuarioId(authentication);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String passwordActual = datos.get("passwordActual");
        String passwordNueva = datos.get("passwordNueva");

        // Verify current password
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña actual es incorrecta"));
        }

        // Validate new password
        if (passwordNueva == null || passwordNueva.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "La nueva contraseña debe tener al menos 6 caracteres"));
        }

        // Update password
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
    }

    private Long obtenerUsuarioId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}
