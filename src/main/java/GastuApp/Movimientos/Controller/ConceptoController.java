package GastuApp.Movimientos.Controller;

import GastuApp.Movimientos.DTO.ConceptoDTO;
import GastuApp.Movimientos.Service.ConceptoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de Conceptos.
 * Reemplaza el MockConceptoController con operaciones reales contra la BD.
 * 
 * Endpoints públicos (lectura):
 * - GET /api/conceptos - Listar todos
 * - GET /api/conceptos/{id} - Obtener por ID
 * - GET /api/conceptos/tipo/{tipo} - Filtrar por tipo
 * 
 * Endpoints de administración (requieren rol ADMIN):
 * - POST /api/conceptos - Crear concepto
 * - PUT /api/conceptos/{id} - Actualizar concepto
 * - DELETE /api/conceptos/{id} - Eliminar concepto
 */
@RestController
@RequestMapping("/api/conceptos")
public class ConceptoController {

    private final ConceptoService conceptoService;

    public ConceptoController(ConceptoService conceptoService) {
        this.conceptoService = conceptoService;
    }

    /**
     * Obtiene todos los conceptos.
     * Endpoint público para que usuarios puedan ver conceptos disponibles.
     *
     * @return Lista de todos los conceptos
     */
    @GetMapping
    public ResponseEntity<List<ConceptoDTO>> listarConceptos() {
        List<ConceptoDTO> conceptos = conceptoService.obtenerTodos();
        return ResponseEntity.ok(conceptos);
    }

    /**
     * Obtiene un concepto por su ID.
     * Usado por IngresoService y EgresoService para validar conceptos.
     *
     * @param id ID del concepto
     * @return DTO del concepto
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConceptoDTO> obtenerConcepto(@PathVariable Long id) {
        ConceptoDTO concepto = conceptoService.obtenerPorId(id);
        return ResponseEntity.ok(concepto);
    }

    /**
     * Obtiene conceptos filtrados por tipo.
     * Útil para mostrar solo conceptos de INGRESO o EGRESO en el frontend.
     *
     * @param tipo Tipo de concepto (INGRESO, EGRESO, AHORRO)
     * @return Lista de conceptos del tipo especificado
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ConceptoDTO>> obtenerPorTipo(@PathVariable String tipo) {
        List<ConceptoDTO> conceptos = conceptoService.obtenerPorTipo(tipo);
        return ResponseEntity.ok(conceptos);
    }

    /**
     * Crea un nuevo concepto.
     * Solo usuarios con rol ADMIN pueden crear conceptos.
     * TODO: Agregar validación de rol ADMIN con @PreAuthorize
     *
     * @param dto DTO con los datos del concepto
     * @return DTO del concepto creado con status 201
     */
    @PostMapping
    public ResponseEntity<ConceptoDTO> crearConcepto(@Valid @RequestBody ConceptoDTO dto) {
        ConceptoDTO conceptoCreado = conceptoService.crearConcepto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(conceptoCreado);
    }

    /**
     * Actualiza un concepto existente.
     * Solo usuarios con rol ADMIN pueden actualizar conceptos.
     * TODO: Agregar validación de rol ADMIN con @PreAuthorize
     *
     * @param id  ID del concepto a actualizar
     * @param dto DTO con los nuevos datos
     * @return DTO del concepto actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConceptoDTO> actualizarConcepto(
            @PathVariable Long id,
            @Valid @RequestBody ConceptoDTO dto) {
        ConceptoDTO conceptoActualizado = conceptoService.actualizarConcepto(id, dto);
        return ResponseEntity.ok(conceptoActualizado);
    }

    /**
     * Elimina un concepto.
     * Solo usuarios con rol ADMIN pueden eliminar conceptos.
     * TODO: Agregar validación de rol ADMIN con @PreAuthorize
     *
     * @param id ID del concepto a eliminar
     * @return Respuesta sin contenido con status 204
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarConcepto(@PathVariable Long id) {
        conceptoService.eliminarConcepto(id);
        return ResponseEntity.noContent().build();
    }
}
