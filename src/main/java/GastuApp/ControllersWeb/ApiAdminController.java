package GastuApp.ControllersWeb;

import GastuApp.User.Usuario;
import GastuApp.User.UsuarioRepository;
import GastuApp.User.RolRepository;
import GastuApp.User.Rol;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class ApiAdminController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiAdminController(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserAdminDTO> listAll() {
        return usuarioRepository.findAll().stream().map(UserAdminDTO::fromEntity).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserAdminDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es requerida"));
        }
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario es requerido"));
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es requerido"));
        }
        // Validate email format
        if (!dto.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo tiene un formato inválido"));
        }
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo ya está registrado"));
        }
        Usuario u = new Usuario();
        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setTelefono(dto.getTelefono());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setActivo(dto.getActivo() == null ? true : dto.getActivo());
        if (dto.getRol() != null) {
            Rol r = rolRepository.findByNombre(dto.getRol());
            if (r != null) u.setRol(r);
        }
        usuarioRepository.save(u);
        return ResponseEntity.status(201).body(UserAdminDTO.fromEntity(u));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(u -> {
            usuarioRepository.delete(u);
            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserAdminDTO dto) {
        return usuarioRepository.findById(id).map(u -> {
            // actualizar campos permitidos
            if (dto.getUsername() != null) u.setUsername(dto.getUsername());
            if (dto.getEmail() != null) u.setEmail(dto.getEmail());
            if (dto.getTelefono() != null) u.setTelefono(dto.getTelefono());
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            if (dto.getActivo() != null) {
                u.setActivo(dto.getActivo());
            }
            if (dto.getRol() != null) {
                Rol r = rolRepository.findByNombre(dto.getRol());
                if (r != null) u.setRol(r);
            }
            usuarioRepository.save(u);
            return ResponseEntity.ok(UserAdminDTO.fromEntity(u));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAdminDTO> getOne(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(u -> ResponseEntity.ok(UserAdminDTO.fromEntity(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DTO para administrar usuarios (no expone password)
    public static class UserAdminDTO {
        private Long id;
        private String username;
        private String email;
        private String telefono;
        private String rol;
        private String password;
        private Boolean activo;

        public static UserAdminDTO fromEntity(Usuario u) {
            UserAdminDTO d = new UserAdminDTO();
            d.id = u.getId();
            d.username = u.getUsername();
            d.email = u.getEmail();
            d.telefono = u.getTelefono();
            d.rol = u.getRol() != null ? u.getRol().getNombre() : null;
            d.activo = u.getActivo();
            return d;
        }

        // getters y setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Boolean getActivo() { return activo; }
        public void setActivo(Boolean activo) { this.activo = activo; }
    }

    @GetMapping("/roles")
    public List<String> listRoles() {
        List<String> roles = rolRepository.findAll().stream().map(Rol::getNombre).collect(Collectors.toList());
        if (roles == null || roles.isEmpty()) {
            // fallback defaults if DB has no roles yet
            return List.of("ADMINISTRADOR", "INSTRUCTOR", "APRENDIZ");
        }
        return roles;
    }
}
