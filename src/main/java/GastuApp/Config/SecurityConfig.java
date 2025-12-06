package GastuApp.Config;

import GastuApp.User.UsuarioDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

        private final JwtUtil jwtUtil;
        private final UsuarioDetailsServiceImpl userDetailsService;

        public SecurityConfig(JwtUtil jwtUtil, UsuarioDetailsServiceImpl userDetailsService) {
                this.jwtUtil = jwtUtil;
                this.userDetailsService = userDetailsService;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // Instanciar filtro manualmente para evitar registro global automático
                JwtFilter jwtFilter = new JwtFilter(jwtUtil, userDetailsService);

                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // Rutas públicas de API
                                                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                                                // Rutas públicas de vistas web
                                                .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                                                .requestMatchers(new AntPathRequestMatcher("/register")).permitAll()
                                                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                                                .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                                                .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
                                                .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                                                // Rutas de API requieren autenticación JWT
                                                .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
                                                // Rutas web requieren autenticación de sesión
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                // Remember-me para sesiones persistentes (30 días)
                                .rememberMe(remember -> remember
                                                .key("gastuapp-remember-me-secret-2024")
                                                .tokenValiditySeconds(60 * 60 * 24 * 30) // 30 días
                                                .rememberMeParameter("remember-me")
                                                .userDetailsService(userDetailsService)
                                                .alwaysRemember(true)) // Siempre recordar sin necesidad de checkbox
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .deleteCookies("JSESSIONID", "remember-me")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .maximumSessions(-1))
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
