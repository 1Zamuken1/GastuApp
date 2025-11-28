package GastuApp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/api/auth/**", "/login", "/register", "/css/**", "/js/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> httpBasic.disable()) // Deshabilitar HTTP Basic
                .formLogin(form -> form.disable()) // no usar login por defecto
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .anonymous(anonymous -> anonymous.disable()); // Deshabilitar acceso anónimo

        // añadir filtro JWT antes del filtro de autenticación por username/password
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtFilter> jwtFilterRegistration(
            JwtFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<JwtFilter> registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(
                filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
