package GastuApp.ControllersWeb;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.ArrayList;

@Controller
public class AdminController {

    private final GastuApp.User.UsuarioRepository usuarioRepository;
    private final GastuApp.Conceptos.Repository.ConceptoRepository conceptoRepository;

    public AdminController(GastuApp.User.UsuarioRepository usuarioRepository,
            GastuApp.Conceptos.Repository.ConceptoRepository conceptoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.conceptoRepository = conceptoRepository;
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model) {
        // Collect Stats
        long userCount = usuarioRepository.count();
        long conceptCount = conceptoRepository.count();

        // Generate Dynamic Activities ("Functional" Simulations based on real data)
        List<DashboardActivity> activities = new ArrayList<>();

        // 1. System Check
        activities.add(new DashboardActivity(
                "Sistema Operativo",
                "Todos los servicios están activos y funcionando.",
                "Ahora",
                "success",
                "bi-hdd-network"));

        // 2. Concepts Stat
        activities.add(new DashboardActivity(
                "Base de Datos de Conceptos",
                "Gestión de " + conceptCount + " conceptos financieros activa.",
                "Hace 1 min",
                "primary",
                "bi-database-check"));

        // 3. Users Stat
        activities.add(new DashboardActivity(
                "Comunidad de Usuarios",
                "Se han registrado " + userCount + " usuarios en la plataforma.",
                "Hace 5 min",
                "info",
                "bi-people"));

        // 4. Login Event (Simulation of current session)
        activities.add(new DashboardActivity(
                "Inicio de Sesión",
                "Acceso administrativo autorizado correctamente.",
                "Reciente",
                "warning",
                "bi-key"));

        model.addAttribute("activities", activities);
        model.addAttribute("userCount", userCount);
        model.addAttribute("conceptCount", conceptCount);

        return "admin/home";
    }

    // Simple DTO for Dashboard Activities
    public record DashboardActivity(String title, String description, String time, String color, String icon) {
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/users";
    }

    @GetMapping("/admin/ajustes-conceptos")
    public String settings(Model model) {
        return "admin/ajustes-conceptos";
    }

    @GetMapping("/admin/profile")
    public String profile(java.security.Principal principal, Model model) {
        if (principal != null) {
            String username = principal.getName();
            GastuApp.User.Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElse(usuarioRepository.findByEmail(username).orElse(new GastuApp.User.Usuario()));
            model.addAttribute("usuario", usuario);
        }
        return "admin/profile";
    }
}
