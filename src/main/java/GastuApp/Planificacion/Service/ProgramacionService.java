package GastuApp.Planificacion.Service;

import GastuApp.Planificacion.DTO.ProgramacionDTO;
import GastuApp.Planificacion.Entities.Programacion;
import GastuApp.Planificacion.Repository.ProgramacionRepository;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.Movimientos.Service.IngresoService;
import GastuApp.Movimientos.Service.EgresoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgramacionService {

    private final ProgramacionRepository programacionRepository;
    private final ConceptoService conceptoService;
    private final IngresoService ingresoService;
    private final EgresoService egresoService;

    public ProgramacionService(
            ProgramacionRepository programacionRepository,
            ConceptoService conceptoService,
            IngresoService ingresoService,
            EgresoService egresoService
    ) {
        this.programacionRepository = programacionRepository;
        this.conceptoService = conceptoService;
        this.ingresoService = ingresoService;
        this.egresoService = egresoService;
    }

    // ---------------------------------------------------------
    //  Convertir entidad → DTO
    // ---------------------------------------------------------
    private ProgramacionDTO entityToDto(Programacion p) {

        String conceptoNombre = conceptoService.obtenerNombrePorId(p.getConceptoId());

        return new ProgramacionDTO(
                p.getId(),
                p.getMontoProgramado(),
                p.getTipo().name(),
                p.getDescripcion(),
                p.getFechaInicio(),
                p.getProximaEjecucion(),
                p.getFrecuencia(),
                p.getConceptoId(),
                conceptoNombre,
                p.isActivo()
        );
    }

    // ---------------------------------------------------------
    //  Convertir DTO → Entidad (solo para crear/editar)
    // ---------------------------------------------------------
    private Programacion dtoToEntity(ProgramacionDTO dto) {
    Programacion p = new Programacion();

    p.setId(dto.getId());
    p.setMontoProgramado(dto.getMontoProgramado());
    p.setDescripcion(dto.getDescripcion());
    p.setFrecuencia(dto.getFrecuencia());
    p.setConceptoId(dto.getConceptoId());
    p.setFechaInicio(dto.getFechaInicio());
    p.setActivo(dto.isActivo());

    // AGREGAR ESTO PARA NO GENERAR ERRORES
    if (dto.getTipo() != null) {
        p.setTipo(Programacion.TipoProgramacion.valueOf(dto.getTipo().toUpperCase()));
    }

    return p;
}


    // ---------------------------------------------------------
    //  Crear nueva programación
    // ---------------------------------------------------------
    @Transactional
    public ProgramacionDTO crear(ProgramacionDTO dto, Long usuarioId) {

        // Validar que el concepto existe y obtener su tipo
        var concepto = conceptoService.obtenerPorId(dto.getConceptoId());
        dto.setTipo(concepto.getTipo()); // INGRESO o EGRESO

        // Convertir DTO a entidad
        Programacion p = dtoToEntity(dto);
        p.setUsuarioId(usuarioId);
        p.setTipo(Programacion.TipoProgramacion.valueOf(dto.getTipo().toUpperCase()));

        // Calcular primera ejecución
        p.setProximaEjecucion(dto.getFechaInicio());

        // Guardar
        p = programacionRepository.save(p);

        return entityToDto(p);
    }

    // ---------------------------------------------------------
    //  Editar una programación
    // ---------------------------------------------------------
    @Transactional
    public ProgramacionDTO editar(Long id, ProgramacionDTO dto, Long usuarioId) {
        Programacion p = programacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programación no encontrada"));

        if (!p.getUsuarioId().equals(usuarioId))
            throw new RuntimeException("No autorizado");

        // Validar concepto
        var concepto = conceptoService.obtenerPorId(dto.getConceptoId());
        dto.setTipo(concepto.getTipo());

        p.setMontoProgramado(dto.getMontoProgramado());
        p.setDescripcion(dto.getDescripcion());
        p.setConceptoId(dto.getConceptoId());
        p.setFrecuencia(dto.getFrecuencia());
        p.setFechaInicio(dto.getFechaInicio());
        p.setProximaEjecucion(dto.getProximaEjecucion());
        p.setTipo(Programacion.TipoProgramacion.valueOf(dto.getTipo().toUpperCase()));

        programacionRepository.save(p);
        return entityToDto(p);
    }

    // ---------------------------------------------------------
    //  Eliminar programación
    // ---------------------------------------------------------
    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        Programacion p = programacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programación no encontrada"));

        if (!p.getUsuarioId().equals(usuarioId))
            throw new RuntimeException("No autorizado");

        programacionRepository.delete(p);
    }

    // ---------------------------------------------------------
    //  Activar / Desactivar programación
    // ---------------------------------------------------------
    @Transactional
    public void cambiarEstado(Long id, Long usuarioId) {
        Programacion p = programacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programación no encontrada"));

        if (!p.getUsuarioId().equals(usuarioId))
            throw new RuntimeException("No autorizado");

        p.setActivo(!p.isActivo());

        // Si se reactiva, reiniciar ejecución a hoy
        if (p.isActivo()) {
            p.setProximaEjecucion(LocalDate.now());
        }

        programacionRepository.save(p);
    }

    // ---------------------------------------------------------
    //  Consultar todas del usuario
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public List<ProgramacionDTO> obtenerPorUsuario(Long usuarioId) {
        return programacionRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    //  Buscar por término (search bar)
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public List<ProgramacionDTO> buscar(Long usuarioId, String termino) {
        return programacionRepository.search(usuarioId, termino)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    //  Obtener programaciones pendientes (para mostrar alertas)
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public List<ProgramacionDTO> obtenerPendientes(Long usuarioId) {
        return programacionRepository
                .findByUsuarioIdAndProximaEjecucion(usuarioId, LocalDate.now())
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    //  Calcular próxima fecha de ejecución según frecuencia
    // ---------------------------------------------------------
    private LocalDate calcularSiguiente(LocalDate actual, String frecuencia) {
        return switch (frecuencia.toUpperCase()) {
            case "DIARIO" -> actual.plusDays(1);
            case "SEMANAL" -> actual.plusWeeks(1);
            case "QUINCENAL" -> actual.plusDays(15);
            case "MENSUAL" -> actual.plusMonths(1);
            case "ANUAL" -> actual.plusYears(1);
            default -> actual;
        };
    }

    // ---------------------------------------------------------
    //  Aceptar una programación (genera ingreso/egreso)
    // ---------------------------------------------------------
    @Transactional
    public void aceptar(Long id, Long usuarioId) {

        Programacion p = programacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programación no encontrada"));

        if (!p.getUsuarioId().equals(usuarioId))
            throw new RuntimeException("No autorizado");

        // Crear ingreso o egreso según tipo
        if (p.getTipo() == Programacion.TipoProgramacion.INGRESO) {
            ingresoService.crearIngreso(
                    new GastuApp.Movimientos.DTO.IngresoDTO(
                            null,
                            p.getMontoProgramado(),
                            p.getDescripcion(),
                            p.getConceptoId(),
                            null
                    ),
                    usuarioId
            );
        } else {
            egresoService.crearEgreso(
                    new GastuApp.Movimientos.DTO.EgresoDTO(
                            null,
                            p.getMontoProgramado(),
                            p.getDescripcion(),
                            p.getConceptoId(),
                            null
                    ),
                    usuarioId
            );
        }

        // Recalcular próxima ejecución
        p.setProximaEjecucion(calcularSiguiente(p.getProximaEjecucion(), p.getFrecuencia()));
        programacionRepository.save(p);
    }

    // ---------------------------------------------------------
    //  Rechazar una programación (solo reprograma la fecha)
    // ---------------------------------------------------------
    @Transactional
    public void rechazar(Long id, Long usuarioId) {

        Programacion p = programacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programación no encontrada"));

        if (!p.getUsuarioId().equals(usuarioId))
            throw new RuntimeException("No autorizado");

        // Solo mover la fecha
        p.setProximaEjecucion(calcularSiguiente(p.getProximaEjecucion(), p.getFrecuencia()));
        programacionRepository.save(p);
    }
}
