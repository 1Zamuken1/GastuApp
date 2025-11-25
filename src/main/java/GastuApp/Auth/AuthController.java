package GastuApp.Auth;

import GastuApp.User.Usuario;
import GastuApp.User.UsuarioService;
import GastuApp.Config.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    // register desde form
    @PostMapping("/register")
    public String register(@RequestParam("nombre") String nombre,
                           @RequestParam("correo") String correo,
                           @RequestParam String password,
                           @RequestParam(required = false) String telefono) {
        if (telefono == null) telefono = "";
        usuarioService.registrar(nombre, correo, password, telefono);
        return "redirect:/login";
    }

    // login desde form
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response) {

        Usuario u = usuarioService.buscarPorUsername(username).orElse(null);
        if (u == null) return "redirect:/login?error";

        // validar password
        org.springframework.security.crypto.password.PasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!encoder.matches(password, u.getPassword())) return "redirect:/login?error";

        String token = jwtUtil.generarToken(u.getUsername());
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (24 * 60 * 60)); // 1 dia
        response.addCookie(cookie);

        // redirigir según rol
        String rolNombre = u.getRol() != null ? u.getRol().getNombre() : "aprendiz";
        if ("administrador".equalsIgnoreCase(rolNombre)) return "redirect:/admin/home";
        if ("instructor".equalsIgnoreCase(rolNombre)) return "redirect:/instructor/home";
        return "redirect:/aprendiz/home";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login";
    }
}
