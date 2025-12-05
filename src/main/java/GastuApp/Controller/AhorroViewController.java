package GastuApp.Controller;

import GastuApp.Ahorro.DTO.AhorroDTO;
import GastuApp.Ahorro.DTO.CrearAhorroDTO;
import GastuApp.Ahorro.DTO.EditarAhorroDTO;
import GastuApp.Ahorro.Service.AhorroService;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.User.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ahorros")
public class AhorroViewController {

    private final AhorroService ahorroService;
    private final ConceptoService conceptoService;

    public AhorroViewController(AhorroService ahorroService, ConceptoService conceptoService) {
        this.ahorroService = ahorroService;
        this.conceptoService = conceptoService;
    }

    @GetMapping
    public String listarAhorros(Model model) {
        model.addAttribute("activePage", "ahorros");
        return "ahorros/ahorro";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("ahorro", new CrearAhorroDTO());
        model.addAttribute("conceptos", conceptoService.obtenerPorTipo("AHORRO"));
        model.addAttribute("activePage", "ahorros");
        return "ahorros/form";
    }

    @PostMapping("/guardar")
    public String guardarAhorro(@ModelAttribute CrearAhorroDTO dto, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        ahorroService.crear(dto, usuarioId);
        return "redirect:/ahorros";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        AhorroDTO ahorro = ahorroService.obtenerPorIdYUsuario(id, usuarioId);

        // Mapeamos AhorroDTO a un objeto compatible con el formulario si es necesario
        // En este caso, usaremos AhorroDTO directamente en la vista, pero el form debe
        // manejar los campos
        // Nota: AhorroDTO tiene 'monto' en lugar de 'montoMeta' y 'meta' en lugar de
        // 'fechaMeta'
        // Necesitamos asegurar que el DTO en el modelo coincida con los campos del form
        // (th:field)
        // O creamos un DTO específico para la vista que combine ambos.
        // Para simplificar, usaremos AhorroDTO y ajustaremos el form.html si es
        // necesario,
        // PERO el form.html usa 'montoMeta', 'fechaMeta', 'cantidadCuotas'.
        // AhorroDTO tiene 'monto', 'meta', 'cantCuotas'.
        // Vamos a crear un objeto wrapper o mapa, o mejor, un DTO ad-hoc.

        // Mejor opción: Usar AhorroDTO y ajustar el form.html para usar los nombres de
        // AhorroDTO?
        // No, porque CrearAhorroDTO usa 'montoMeta'.
        // Vamos a mapear manualmente a una clase que extienda CrearAhorroDTO o similar.
        // O simplemente pasamos el AhorroDTO y en el POST de actualizar recibimos
        // EditarAhorroDTO.

        // Vamos a pasar AhorroDTO pero con los getters que coincidan con el form?
        // El form usa *{montoMeta}. AhorroDTO tiene getMonto().
        // Esto va a fallar.
        // Solución: Crear una instancia de CrearAhorroDTO (o una clase híbrida) y
        // llenarla con los datos del AhorroDTO.

        CrearAhorroDTO formDto = new CrearAhorroDTO();
        formDto.setConceptoId(ahorro.getConceptoId());
        formDto.setDescripcion(ahorro.getDescripcion());
        formDto.setMontoMeta(ahorro.getMonto());
        formDto.setFrecuencia(ahorro.getFrecuencia());
        formDto.setFechaMeta(ahorro.getMeta());
        formDto.setCantidadCuotas(ahorro.getCantCuotas());

        // Hack: Necesitamos el ID para saber que es edición. CrearAhorroDTO no tiene
        // ID.
        // Vamos a añadir el ID al modelo por separado o usar un DTO que tenga ID.
        // Usemos AhorroDTO y cambiemos el form para que sea agnóstico o use nombres
        // comunes?
        // No, cambiemos el form para que use los nombres de CrearAhorroDTO, y aquí
        // pasamos un objeto que tenga esos nombres + ID.

        // Vamos a usar una clase interna o un Map.
        // O mejor, pasamos el AhorroDTO y en el form usamos th:value="${ahorro.monto}"
        // en vez de th:field si los nombres no coinciden?
        // No, th:field es mejor.

        // Vamos a usar AhorroDTO y añadir alias en AhorroDTO? No puedo modificar DTOs
        // fácilmente aquí sin tool calls.
        // Vamos a usar CrearAhorroDTO y pasar el ID en el modelo separado.

        model.addAttribute("ahorro", formDto);
        model.addAttribute("ahorroId", id); // Para el th:action
        model.addAttribute("conceptos", conceptoService.obtenerPorTipo("AHORRO"));
        model.addAttribute("activePage", "ahorros");
        return "ahorros/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarAhorro(@PathVariable Long id, @ModelAttribute EditarAhorroDTO dto,
            Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        ahorroService.actualizar(id, dto, usuarioId);
        return "redirect:/ahorros";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarAhorro(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = obtenerUsuarioId(authentication);
        ahorroService.eliminar(id, usuarioId);
        return "redirect:/ahorros";
    }

    private Long obtenerUsuarioId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}
