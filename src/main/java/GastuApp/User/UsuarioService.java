package GastuApp.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepo, RolRepository rolRepo, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.rolRepo = rolRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initRoles() {
        if (rolRepo.findByNombre("administrador") == null) rolRepo.save(new Rol("administrador"));
        if (rolRepo.findByNombre("instructor") == null) rolRepo.save(new Rol("instructor"));
        if (rolRepo.findByNombre("aprendiz") == null) rolRepo.save(new Rol("aprendiz"));
    }

    public Usuario registrar(String username, String email, String password, String telefono) {
        if (usuarioRepo.existsByUsername(username)) throw new RuntimeException("username exists");
        if (usuarioRepo.existsByEmail(email)) throw new RuntimeException("email exists");

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setTelefono(telefono);
        // Assign default role "aprendiz" on registration
        Rol rol = rolRepo.findByNombre("aprendiz");
        if (rol == null) rol = rolRepo.save(new Rol("aprendiz"));
        u.setRol(rol);

        return usuarioRepo.save(u);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepo.findByUsername(username);
    }
}
