package GastuApp.Controller;

import GastuApp.Movimientos.DTO.IngresoDTO;
import GastuApp.Movimientos.Service.ConceptoService;
import GastuApp.Movimientos.Service.IngresoService;
import GastuApp.User.CustomUserDetails;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/movimientos/ingresos")
public class IngresoViewController {

    private final IngresoService ingresoService;
    private final ConceptoService conceptoService;

    public IngresoViewController(IngresoService ingresoService, ConceptoService conceptoService) {
        this.ingresoService = ingresoService;
        this.conceptoService = conceptoService;
    }

    @GetMapping
    public String listarIngresos(Model model) {
        // Fast page load - data will be fetched via optimized API calls
        model.addAttribute("activePage", "ingresos");
        return "movimientos/ingresos/ingresos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("ingreso", new IngresoDTO());
        model.addAttribute("conceptos", conceptoService.obtenerPorTipo("INGRESO"));
        model.addAttribute("activePage", "ingresos");
        return "ingresos/form";
    }

    @PostMapping("/guardar")
    public String guardarIngreso(@ModelAttribute IngresoDTO ingresoDTO, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        ingresoService.crearIngreso(ingresoDTO, usuarioId);
        return "redirect:/movimientos/ingresos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        IngresoDTO ingreso = ingresoService.obtenerIngresoPorId(id, usuarioId);
        model.addAttribute("ingreso", ingreso);
        model.addAttribute("conceptos", conceptoService.obtenerPorTipo("INGRESO"));
        model.addAttribute("activePage", "ingresos");
        return "ingresos/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarIngreso(@PathVariable Long id, @ModelAttribute IngresoDTO ingresoDTO,
            Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        ingresoService.actualizarIngreso(id, ingresoDTO, usuarioId);
        return "redirect:/ingresos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarIngreso(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        ingresoService.eliminarIngreso(id, usuarioId);
        return "redirect:/ingresos";
    }

    private Long obtenerUsuarioId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}
