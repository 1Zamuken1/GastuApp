package GastuApp.Planificacion.Service;

import GastuApp.Planificacion.DTO.PresupuestoDTO;
import GastuApp.Planificacion.Entities.Presupuesto;
import GastuApp.Planificacion.Repository.PresupuestoRepository;
import GastuApp.Conceptos.Service.ConceptoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final ConceptoService conceptoService;

    public PresupuestoService(PresupuestoRepository presupuestoRepository, ConceptoService conceptoService) {
        this.presupuestoRepository = presupuestoRepository;
        this.conceptoService = conceptoService;
    }

    // ---------------- Mapeadores ----------------

    private Presupuesto toEntity(PresupuestoDTO dto) {
        Presupuesto p = new Presupuesto();
        p.setId(dto.getId());
        p.setLimite(dto.getLimite());
        p.setFechaInicio(dto.getFechaInicio());
        p.setFechaFin(dto.getFechaFin());
        p.setActivo(dto.getActivo());
        p.setConceptoId(dto.getConceptoId());
        return p;
    }

    private PresupuestoDTO toDTO(Presupuesto p) {
        PresupuestoDTO dto = new PresupuestoDTO();
        dto.setId(p.getId());
        dto.setLimite(p.getLimite());
        dto.setFechaInicio(p.getFechaInicio());
        dto.setFechaFin(p.getFechaFin());
        dto.setActivo(p.getActivo());
        dto.setFechaCreacion(p.getFechaCreacion());
        dto.setConceptoId(p.getConceptoId());

        // Cargar nombre del concepto
        if (p.getConceptoId() != null) {
            try {
                dto.setConceptoNombre(conceptoService.obtenerPorId(p.getConceptoId()).getNombre());
            } catch (RuntimeException e) {
                dto.setConceptoNombre("Concepto no encontrado");
            }
        }
        return dto;
    }

    // ---------------- CRUD con usuarioId explícito ----------------

    @Transactional(readOnly = true)
    public List<PresupuestoDTO> listarTodosPorUsuario(Long usuarioId) {
        return presupuestoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PresupuestoDTO obtenerPorIdYUsuario(Long id, Long usuarioId) {
        Presupuesto presupuesto = presupuestoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado o sin permisos"));
        return toDTO(presupuesto);
    }

    @Transactional
    public PresupuestoDTO crear(PresupuestoDTO dto, Long usuarioId) {
        // Validaciones
        validarConcepto(dto.getConceptoId());
        
        // Valores por defecto
        if (dto.getActivo() == null) dto.setActivo(true);
        if (dto.getFechaInicio() == null) dto.setFechaInicio(LocalDate.now());

        // Crear entidad
        Presupuesto entity = toEntity(dto);
        entity.setUsuarioId(usuarioId);

        Presupuesto guardado = presupuestoRepository.save(entity);
        return toDTO(guardado);
    }

    @Transactional
    public PresupuestoDTO actualizar(Long id, PresupuestoDTO dto, Long usuarioId) {
        // Validar que existe y pertenece al usuario
        Presupuesto existente = presupuestoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado o sin permisos"));

        // FIX: Validar concepto con manejo de nulos
        if (dto.getConceptoId() == null) {
            throw new RuntimeException("El concepto es requerido");
        }
        
        // Solo validar si el concepto cambió
        if (existente.getConceptoId() == null || !existente.getConceptoId().equals(dto.getConceptoId())) {
            validarConcepto(dto.getConceptoId());
        }

        // Actualizar campos
        existente.setLimite(dto.getLimite());
        existente.setFechaInicio(dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now());
        existente.setFechaFin(dto.getFechaFin());
        existente.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        existente.setConceptoId(dto.getConceptoId());

        Presupuesto actualizado = presupuestoRepository.save(existente);
        return toDTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        Presupuesto existente = presupuestoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado o sin permisos"));
        presupuestoRepository.delete(existente);
    }

    // --------- Validaciones ---------

    private void validarConcepto(Long conceptoId) {
        if (conceptoId == null) {
            throw new RuntimeException("El concepto es requerido");
        }
        conceptoService.obtenerPorId(conceptoId); 
    }
}