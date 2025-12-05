package GastuApp.Planificacion.Service;

import GastuApp.Planificacion.DTO.PresupuestoDTO;
import GastuApp.Planificacion.Entities.Presupuesto;
import GastuApp.Planificacion.Repository.PresupuestoRepository;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.Movimientos.Repository.MovimientoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final ConceptoService conceptoService;
    private final MovimientoRepository movimientoRepository;

    public PresupuestoService(
            PresupuestoRepository presupuestoRepository,
            ConceptoService conceptoService,
            MovimientoRepository movimientoRepository
    ) {
        this.presupuestoRepository = presupuestoRepository;
        this.conceptoService = conceptoService;
        this.movimientoRepository = movimientoRepository;
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

        try {
            dto.setConceptoNombre(conceptoService.obtenerNombrePorId(p.getConceptoId()));
        } catch (Exception e) {
            dto.setConceptoNombre("Concepto no encontrado");
        }

        return dto;
    }

    // ---------------- CRUD ----------------

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

        // Validar duplicados activos
        validarConceptoDisponible(dto.getConceptoId(), usuarioId);

        if (dto.getActivo() == null) dto.setActivo(true);
        if (dto.getFechaInicio() == null) dto.setFechaInicio(LocalDate.now());

        Presupuesto entity = toEntity(dto);
        entity.setUsuarioId(usuarioId);

        Presupuesto guardado = presupuestoRepository.save(entity);

        return toDTO(guardado);
    }

    @Transactional
    public PresupuestoDTO actualizar(Long id, PresupuestoDTO dto, Long usuarioId) {

        Presupuesto existente = presupuestoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado o sin permisos"));

        if (dto.getConceptoId() == null)
            throw new RuntimeException("El concepto es requerido");

        // Si el concepto cambia, validar duplicado
        if (!existente.getConceptoId().equals(dto.getConceptoId())) {
            validarConceptoDisponible(dto.getConceptoId(), usuarioId);
        }

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

    private void validarConceptoDisponible(Long conceptoId, Long usuarioId) {

        List<Presupuesto> activos = presupuestoRepository
                .findByUsuarioIdAndConceptoIdAndActivoTrue(usuarioId, conceptoId);

        if (!activos.isEmpty()) {
            throw new IllegalStateException("Ya existe un presupuesto ACTIVO para este concepto.");
        }
    }

    // ************************************************************
    // *** CÁLCULO DE GASTOS Y PROGRESO DEL LIMITE ***
    // ************************************************************

    @Transactional(readOnly = true)
    public List<PresupuestoDTO> obtenerPresupuestosConProgreso(Long usuarioId) {

        List<Presupuesto> presupuestos =
                presupuestoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);

        return presupuestos.stream()
                .map(this::mapearProgreso)
                .collect(Collectors.toList());
    }

    private PresupuestoDTO mapearProgreso(Presupuesto p) {

        LocalDateTime inicio = p.getFechaInicio().atStartOfDay();
        LocalDateTime fin = (p.getFechaFin() != null)
                ? p.getFechaFin().atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        var gastado = movimientoRepository.sumarEgresosPorConceptoYRango(
                p.getUsuarioId(),
                p.getConceptoId(),
                inicio,
                fin
        );

        PresupuestoDTO dto = toDTO(p);
        dto.setGastado(gastado.doubleValue());
        return dto;
    }

    // -------- Activar Presupuesto --------

    @Transactional
    public PresupuestoDTO activarPresupuesto(Long id, Long usuarioId) {

        Presupuesto p = presupuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        if (!p.getUsuarioId().equals(usuarioId))
            throw new RuntimeException("No autorizado");

        if (p.getActivo())
            return toDTO(p);

        List<Presupuesto> activos =
                presupuestoRepository.findByUsuarioIdAndConceptoIdAndActivoTrue(usuarioId, p.getConceptoId());

        boolean existeOtro = activos.stream()
                .anyMatch(x -> !x.getId().equals(p.getId()));

        if (existeOtro) {
            throw new IllegalStateException("Ya existe un presupuesto ACTIVO para este concepto.");
        }

        p.setActivo(true);

        Presupuesto guardado = presupuestoRepository.save(p);
        return toDTO(guardado);
    }
}
