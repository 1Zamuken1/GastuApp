package GastuApp.Controller;

import GastuApp.Movimientos.DTO.EgresoDTO;
import GastuApp.Movimientos.Service.ConceptoService;
import GastuApp.Movimientos.Service.EgresoService;
import GastuApp.User.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/movimientos/egresos")
public class EgresoViewController {

    private final EgresoService egresoService;
    private final ConceptoService conceptoService;

    public EgresoViewController(EgresoService egresoService, ConceptoService conceptoService) {
        this.egresoService = egresoService;
        this.conceptoService = conceptoService;
    }

    @GetMapping
    public String listarEgresos(Model model) {
        // La nueva vista es SPA - carga datos via AJAX desde la API REST
        model.addAttribute("activePage", "egresos");
        return "movimientos/egresos/egresos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("egreso", new EgresoDTO());
        model.addAttribute("conceptos", conceptoService.obtenerPorTipo("EGRESO"));
        model.addAttribute("activePage", "egresos");
        return "egresos/form";
    }

    @PostMapping("/guardar")
    public String guardarEgreso(@ModelAttribute EgresoDTO egresoDTO, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        egresoService.crearEgreso(egresoDTO, usuarioId);
        return "redirect:/movimientos/egresos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        EgresoDTO egreso = egresoService.obtenerEgresoPorId(id, usuarioId);
        model.addAttribute("egreso", egreso);
        model.addAttribute("conceptos", conceptoService.obtenerPorTipo("EGRESO"));
        model.addAttribute("activePage", "egresos");
        return "egresos/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarEgreso(@PathVariable Long id, @ModelAttribute EgresoDTO egresoDTO,
            Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        egresoService.actualizarEgreso(id, egresoDTO, usuarioId);
        return "redirect:/movimientos/egresos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEgreso(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        egresoService.eliminarEgreso(id, usuarioId);
        return "redirect:/movimientos/egresos";
    }

    private Long obtenerUsuarioId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}
