package GastuApp.Planificacion.Controller;

import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.User.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/programaciones")
public class ProgramacionControllerMVC {

    private final ConceptoService conceptoService;

    public ProgramacionControllerMVC(ConceptoService conceptoService) {
        this.conceptoService = conceptoService;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {

        // Validar sesión
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }

        // Cargar conceptos para el formulario
        try {
            model.addAttribute("conceptosIngreso", conceptoService.obtenerPorTipo("INGRESO"));
            model.addAttribute("conceptosEgreso", conceptoService.obtenerPorTipo("EGRESO"));
        } catch (Exception e) {
            model.addAttribute("conceptosIngreso", java.util.Collections.emptyList());
            model.addAttribute("conceptosEgreso", java.util.Collections.emptyList());
        }

        model.addAttribute("activePage", "programaciones");

        return "planificacion/indexProgramacion";
    }
}
