package GastuApp.Config;

import GastuApp.User.UsuarioDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// NOTA: No usamos @Component para evitar el registro automático en la cadena global de filtros.
// Se instancia manualmente en SecurityConfig.
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsServiceImpl userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, UsuarioDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        System.out.println("DEBUG JwtFilter: Procesando URI: " + uri);

        // Solo aplicar JWT filter a rutas de API
        if (!uri.startsWith("/api/")) {
            System.out.println("DEBUG JwtFilter: No es ruta API, saltando filtro JWT");
            filterChain.doFilter(request, response);
            return;
        }

        // Rutas públicas de API - permitir sin autenticación
        if (uri.startsWith("/api/auth/") || uri.equals("/api/movimientos/ingresos/test")) {
            System.out.println("DEBUG JwtFilter: Ruta API pública, permitiendo sin autenticación");
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Buscar token en header Authorization (para APIs REST)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("DEBUG JwtFilter: Token encontrado en header Authorization");
        }

        if (token == null) {
            System.out.println("DEBUG JwtFilter: No se encontró token en header Authorization");
        }

        // Validar y establecer autenticación
        if (token != null && jwtUtil.validarToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            System.out.println("DEBUG JwtFilter: Token válido para usuario: " + username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("DEBUG JwtFilter: UserDetails cargado: " + userDetails.getClass().getName());
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("DEBUG JwtFilter: Autenticación establecida exitosamente");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("DEBUG JwtFilter: Token inválido o ausente para URI de API: " + uri);
        System.out.println("DEBUG JwtFilter: Continuando cadena de filtros (posible autenticación por sesión)");

        // No bloquear aquí. Permitir que la cadena continúe.
        // Si no hay otra autenticación (sesión), SecurityConfig se encargará de
        // rechazarla.
        filterChain.doFilter(request, response);
    }
}