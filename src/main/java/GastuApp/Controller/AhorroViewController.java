package GastuApp.Controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import GastuApp.Ahorro.DTO.AhorroDTO;
import GastuApp.Ahorro.DTO.CrearAhorroDTO;
import GastuApp.Ahorro.DTO.EditarAhorroDTO;
import GastuApp.Ahorro.Service.AhorroService;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.User.CustomUserDetails;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/ahorros")
public class AhorroViewController {

    private static final String ACTIVE_PAGE = "activePage";
    private static final String AHORROS = "ahorros";
    private static final String AHORROS_FORM = "ahorros/form";
    private static final String AHORROS_LIST = "ahorros/list";
    private static final String AHORROS_AHORRO = "ahorros/ahorro";
    private static final String REDIRECT_AHORROS = "redirect:/ahorros";
    private static final String REDIRECT_AHORROS_LISTA = "redirect:/ahorros/lista";
    private static final String MODEL_AHORRO = "ahorro";
    private static final String MODEL_CONCEPTOS = "conceptos";
    private static final String TIPO_AHORRO = "AHORRO";

    private final AhorroService ahorroService;
    private final ConceptoService conceptoService;

    public AhorroViewController(AhorroService ahorroService, ConceptoService conceptoService) {
        this.ahorroService = ahorroService;
        this.conceptoService = conceptoService;
    }

    // Obtener usuarioId autenticado
    private Long obtenerUsuarioId(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getId();
    }

    // Vista SPA - Ahorros interactivo
    @GetMapping
    public String vistaAhorros(Model model) {
        model.addAttribute(ACTIVE_PAGE, AHORROS);
        return AHORROS_AHORRO;
    }

    // Vista tabla - Listar todos los ahorros
    @GetMapping("/lista")
    public String listaAhorros(
        @RequestParam(required = false) String estado,
        Model model,
        Authentication authentication
    ) {
        Long usuarioId = obtenerUsuarioId(authentication);
        
        List<AhorroDTO> ahorros;
        if (estado != null && !estado.isEmpty()) {
            try {
                ahorros = ahorroService.filtrarPorEstado(usuarioId, 
                    GastuApp.Ahorro.Entity.AhorroMeta.Estado.valueOf(estado.toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                ahorros = ahorroService.listarTodosPorUsuario(usuarioId);
            }
        } else {
            ahorros = ahorroService.listarTodosPorUsuario(usuarioId);
        }
        
        model.addAttribute(MODEL_AHORRO + "s", ahorros);
        model.addAttribute(ACTIVE_PAGE, AHORROS);
        return AHORROS_LIST;
    }

    // Formulario crear nuevo ahorro
    @GetMapping("/nuevo")
    public String nuevoAhorro(Model model, Authentication authentication) {
        obtenerUsuarioId(authentication); // Validar autenticación
        
        model.addAttribute(MODEL_AHORRO, new CrearAhorroDTO());
        model.addAttribute(MODEL_CONCEPTOS, conceptoService.obtenerPorTipo(TIPO_AHORRO));
        model.addAttribute(ACTIVE_PAGE, AHORROS);
        return AHORROS_FORM;
    }

    // Formulario editar ahorro
    @GetMapping("/editar/{id}")
    public String editarAhorro(
        @PathVariable Long id,
        Model model,
        Authentication authentication
    ) {
        Long usuarioId = obtenerUsuarioId(authentication);
        AhorroDTO ahorro = ahorroService.obtenerPorIdYUsuario(id, usuarioId);
        
        if (ahorro == null) {
            return REDIRECT_AHORROS;
        }
        
        // Mapear DTO a formulario
        EditarAhorroDTO formulario = new EditarAhorroDTO();
        formulario.setDescripcion(ahorro.getDescripcion());
        formulario.setMontoMeta(ahorro.getMonto()); // Usar getMonto() que devuelve montoMeta
        formulario.setFrecuencia(ahorro.getFrecuencia());
        formulario.setFechaMeta(ahorro.getMeta()); // Usar getMeta() que devuelve fechaMeta
        formulario.setCantidadCuotas(ahorro.getCantCuotas()); // Usar getCantCuotas()
        
        model.addAttribute(MODEL_AHORRO, formulario);
        model.addAttribute("ahorroId", id); // ID para usar en el form
        model.addAttribute(MODEL_CONCEPTOS, conceptoService.obtenerPorTipo(TIPO_AHORRO));
        model.addAttribute(ACTIVE_PAGE, AHORROS);
        return AHORROS_FORM;
    }

    // Guardar nuevo ahorro (POST form tradicional)
    @PostMapping("/guardar")
    public String guardarAhorro(
        CrearAhorroDTO dto,
        Model model,
        Authentication authentication
    ) {
        try {
            Long usuarioId = obtenerUsuarioId(authentication);
            ahorroService.crear(dto, usuarioId);
            return REDIRECT_AHORROS;
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el ahorro: " + e.getMessage());
            model.addAttribute(MODEL_AHORRO, dto);
            model.addAttribute(MODEL_CONCEPTOS, conceptoService.obtenerPorTipo(TIPO_AHORRO));
            return AHORROS_FORM;
        }
    }

    // Actualizar ahorro (POST form tradicional)
    @PostMapping("/actualizar/{id}")
    public String actualizarAhorro(
        @PathVariable Long id,
        EditarAhorroDTO dto,
        Model model,
        Authentication authentication
    ) {
        try {
            Long usuarioId = obtenerUsuarioId(authentication);
            ahorroService.actualizar(id, dto, usuarioId);
            return REDIRECT_AHORROS;
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el ahorro: " + e.getMessage());
            model.addAttribute(MODEL_AHORRO, dto);
            model.addAttribute(MODEL_CONCEPTOS, conceptoService.obtenerPorTipo(TIPO_AHORRO));
            return AHORROS_FORM;
        }
    }

    // Ver detalles (redirige al SPA)
    @GetMapping("/ver/{id}")
    public String verDetalles(@PathVariable Long id) {
        return REDIRECT_AHORROS;
    }

    // Eliminar ahorro (redirige después de llamar a API)
    @GetMapping("/eliminar/{id}")
    public String eliminarAhorro(
        @PathVariable Long id,
        Authentication authentication
    ) {
        try {
            Long usuarioId = obtenerUsuarioId(authentication);
            ahorroService.eliminar(id, usuarioId);
        } catch (Exception e) {
            // Log error silenciosamente
        }
        return REDIRECT_AHORROS_LISTA;
    }
}
