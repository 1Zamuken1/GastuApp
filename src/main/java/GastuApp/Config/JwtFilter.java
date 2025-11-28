package GastuApp.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final GastuApp.User.UsuarioDetailsServiceImpl userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, GastuApp.User.UsuarioDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        System.out.println("DEBUG JwtFilter: Procesando URI: " + uri);

        // Rutas publicas - permitir sin autenticacion
        if (uri.startsWith("/css") || uri.startsWith("/js") ||
                uri.equals("/login") || uri.equals("/register") ||
                uri.startsWith("/auth/") || uri.startsWith("/api/auth/") ||
                uri.equals("/api/movimientos/ingresos/test")) {
            System.out.println("DEBUG JwtFilter: Ruta pública, permitiendo sin autenticación");
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // OPCION 1: Buscar token en cookies (para vistas web)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("token".equals(c.getName())) {
                    token = c.getValue();
                    System.out.println("DEBUG JwtFilter: Token encontrado en cookie");
                    break;
                }
            }
        }

        // OPCION 2: Buscar token en header Authorization (para APIs REST)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                System.out.println("DEBUG JwtFilter: Token encontrado en header Authorization");
            }
        }

        if (token == null) {
            System.out.println("DEBUG JwtFilter: No se encontró token");
        }

        // Validar y establecer autenticacion
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

        System.out.println("DEBUG JwtFilter: Token inválido o ausente para URI: " + uri);

        // Si es una ruta de API y no hay token valido, devolver 401
        if (uri.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token no valido o ausente\"}");
            return;
        }

        // Para rutas web sin token, redirigir al login
        response.sendRedirect("/login");
    }
}