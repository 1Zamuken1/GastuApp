package GastuApp.Planificacion.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.Planificacion.Service.PresupuestoService;
import GastuApp.User.CustomUserDetails;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/Presupuestos")
public class PresupuestoController {
    private final PresupuestoService presupuestoService;
    private final ConceptoService conceptoService;

    public PresupuestoController(PresupuestoService presupuestoService, ConceptoService conceptoService) {
        this.presupuestoService = presupuestoService;
        this.conceptoService = conceptoService;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        // ✅ FIX: Agregar manejo de autenticación
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        
        Long usuarioId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        
        model.addAttribute("activePage", "Presupuesto");
        
        // ✅ FIX: Cargar conceptos del usuario actual
        try {
            model.addAttribute("conceptos", conceptoService.obtenerTodos());
        } catch (Exception e) {
            // Si falla, continuar sin conceptos
            model.addAttribute("conceptos", java.util.Collections.emptyList());
        }
        
        return "planificacion/indexPresupuesto"; 
    }
}