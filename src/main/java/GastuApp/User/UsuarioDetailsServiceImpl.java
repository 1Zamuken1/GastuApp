package GastuApp.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDetailsServiceImpl.class);

    private final UsuarioRepository usuarioRepo;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Intentar buscar por email primero (soporta login por correo),
        // si no existe, buscar por username para compatibilidad hacia atrás.
        logger.debug("loadUserByUsername recibido: {}", username);
        Usuario user = usuarioRepo.findByEmail(username)
            .orElseGet(() -> usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado")));
        logger.debug("Usuario encontrado para autenticación: id={}, username={}, email={}", user.getId(), user.getUsername(), user.getEmail());

        String roleName = user.getRol() != null ? user.getRol().getNombre() : "aprendiz";
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities);
    }
}
