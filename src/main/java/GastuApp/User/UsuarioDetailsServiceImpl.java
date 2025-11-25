package GastuApp.User;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String[] roles = new String[] { user.getRol() != null ? user.getRol().getNombre() : "aprendiz" };

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }
}
