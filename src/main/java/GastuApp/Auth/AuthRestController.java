package GastuApp.Auth;

import GastuApp.Config.JwtUtil;
import GastuApp.User.Usuario;
import GastuApp.User.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para autenticación de usuarios.
 * Proporciona endpoints JSON para login y obtención de tokens JWT.
 * Diseñado para ser consumido por clientes REST (Postman, frontend, etc.)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthRestController(UsuarioService usuarioService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Endpoint de login que devuelve un token JWT en formato JSON.
     * 
     * @param request Credenciales del usuario (username y password)
     * @return Token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Buscar usuario por username
        Usuario usuario = usuarioService.buscarPorUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar password
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciales inválidas");
            return ResponseEntity.status(401).body(error);
        }

        // Generar token JWT
        String token = jwtUtil.generarToken(usuario.getUsername());

        // Preparar respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", usuario.getUsername());
        response.put("userId", usuario.getId());
        response.put("email", usuario.getEmail());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de registro que crea un nuevo usuario y devuelve un token JWT.
     * 
     * @param request Datos del nuevo usuario
     * @return Token JWT y datos del usuario creado
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Registrar usuario
            Usuario usuario = usuarioService.registrar(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getTelefono() != null ? request.getTelefono() : "");

            // Generar token JWT
            String token = jwtUtil.generarToken(usuario.getUsername());

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", usuario.getUsername());
            response.put("userId", usuario.getId());
            response.put("email", usuario.getEmail());
            response.put("message", "Usuario registrado exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    /**
     * DTO para la petición de login.
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * DTO para la petición de registro.
     */
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String telefono;

        public RegisterRequest() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }
    }
}
