package GastuApp.Movimientos.Service;

import GastuApp.Conceptos.DTO.ConceptoDTO;
import GastuApp.Movimientos.DTO.EgresoDTO;
import GastuApp.Movimientos.DTO.PreferenciasFinancierasDTO;
import GastuApp.Movimientos.Entity.Movimiento;
import GastuApp.Movimientos.Entity.Movimiento.TipoMovimiento;
import GastuApp.Notificaciones.Entity.Notificacion.TipoNotificacion;
import GastuApp.Conceptos.Service.ConceptoService;
import GastuApp.Notificaciones.Service.NotificacionService;
import GastuApp.Notificaciones.Service.AlertAnalysisService;
import GastuApp.Movimientos.Repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que contiene la lógica de negocio para la gestión de Egresos.
 * Incluye validaciones de salud financiera y generación de notificaciones.
 */
@Service
public class EgresoService {

    private final MovimientoRepository movimientoRepository;
    private final NotificacionService notificacionService;
    private final PreferenciasUsuarioService preferenciasService;
    private final ConceptoService conceptoService;
    private final AlertAnalysisService alertAnalysisService;

    public EgresoService(MovimientoRepository movimientoRepository,
            NotificacionService notificacionService,
            PreferenciasUsuarioService preferenciasService,
            ConceptoService conceptoService,
            AlertAnalysisService alertAnalysisService) {
        this.movimientoRepository = movimientoRepository;
        this.notificacionService = notificacionService;
        this.preferenciasService = preferenciasService;
        this.conceptoService = conceptoService;
        this.alertAnalysisService = alertAnalysisService;
    }

    /**
     * Obtiene todos los egresos de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return Lista de DTOs de egresos ordenados por fecha descendente
     */
    @Transactional(readOnly = true)
    public List<EgresoDTO> obtenerEgresosPorUsuario(Long usuarioId) {
        List<Movimiento> egresos = movimientoRepository
                .findByUsuarioIdAndTipoOrderByFechaRegistroDesc(usuarioId, TipoMovimiento.EGRESO);

        return egresos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un egreso específico por su ID.
     * Valida que el egreso pertenezca al usuario solicitante.
     *
     * @param id        ID del egreso
     * @param usuarioId ID del usuario solicitante
     * @return DTO del egreso
     * @throws RuntimeException si el egreso no existe o no pertenece al usuario
     */
    @Transactional(readOnly = true)
    public EgresoDTO obtenerEgresoPorId(Long id, Long usuarioId) {
        Movimiento egreso = movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Egreso no encontrado o no tiene permisos para acceder"));

        if (egreso.getTipo() != TipoMovimiento.EGRESO) {
            throw new RuntimeException("El movimiento solicitado no es un egreso");
        }

        return convertirADTO(egreso);
    }

    /**
     * Crea un nuevo egreso para un usuario.
     * Valida que el concepto seleccionado exista y sea de tipo EGRESO.
     * Realiza validaciones de salud financiera y genera notificaciones si es
     * necesario.
     *
     * @param dto       DTO con los datos del egreso
     * @param usuarioId ID del usuario que crea el egreso
     * @param token     Token JWT (Ignorado, mantenido por compatibilidad temporal)
     * @return DTO del egreso creado
     * @throws RuntimeException si el concepto no existe o no es válido
     */
    @Transactional
    public EgresoDTO crearEgreso(EgresoDTO dto, Long usuarioId) {
        // Validar que el concepto existe y es de tipo EGRESO
        validarConcepto(dto.getConceptoId(), "EGRESO");

        // Crear entidad
        Movimiento egreso = new Movimiento();
        egreso.setTipo(TipoMovimiento.EGRESO);
        egreso.setMonto(dto.getMonto());
        egreso.setDescripcion(dto.getDescripcion());
        egreso.setUsuarioId(usuarioId);
        egreso.setConceptoId(dto.getConceptoId());

        // Guardar
        Movimiento egresoGuardado = movimientoRepository.save(egreso);

        // Analizar y generar alertas asíncronamente
        alertAnalysisService.analizarMovimiento(usuarioId, egresoGuardado, TipoMovimiento.EGRESO);

        return convertirADTO(egresoGuardado);
    }

    /**
     * Actualiza un egreso existente.
     * Valida que el egreso pertenezca al usuario y que el concepto sea válido.
     *
     * @param id        ID del egreso a actualizar
     * @param dto       DTO con los nuevos datos
     * @param usuarioId ID del usuario que actualiza
     * @param token     Token JWT (Ignorado, mantenido por compatibilidad temporal)
     * @return DTO del egreso actualizado
     * @throws RuntimeException si el egreso no existe, no pertenece al usuario o
     *                          el concepto no es válido
     */
    @Transactional
    public EgresoDTO actualizarEgreso(Long id, EgresoDTO dto, Long usuarioId) {
        // Buscar egreso existente
        Movimiento egreso = movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Egreso no encontrado o no tiene permisos para modificarlo"));

        if (egreso.getTipo() != TipoMovimiento.EGRESO) {
            throw new RuntimeException("El movimiento no es un egreso");
        }

        // Validar nuevo concepto si cambió
        if (!egreso.getConceptoId().equals(dto.getConceptoId())) {
            validarConcepto(dto.getConceptoId(), "EGRESO");
        }

        // Actualizar campos
        egreso.setMonto(dto.getMonto());
        egreso.setDescripcion(dto.getDescripcion());
        egreso.setConceptoId(dto.getConceptoId());

        // Guardar cambios
        Movimiento egresoActualizado = movimientoRepository.save(egreso);

        // Re-analizar alertas tras actualización
        alertAnalysisService.analizarMovimiento(usuarioId, egresoActualizado, TipoMovimiento.EGRESO);

        return convertirADTO(egresoActualizado);
    }

    /**
     * Elimina un egreso.
     * Valida que el egreso pertenezca al usuario solicitante.
     *
     * @param id        ID del egreso a eliminar
     * @param usuarioId ID del usuario que solicita la eliminación
     * @throws RuntimeException si el egreso no existe o no pertenece al usuario
     */
    @Transactional
    public void eliminarEgreso(Long id, Long usuarioId) {
        Movimiento egreso = movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Egreso no encontrado o no tiene permisos para eliminarlo"));

        if (egreso.getTipo() != TipoMovimiento.EGRESO) {
            throw new RuntimeException("El movimiento no es un egreso");
        }

        movimientoRepository.delete(egreso);
    }

    /**
     * Obtiene un resumen de conceptos de EGRESO con estadísticas (cantidad y
     * total).
     * Solo incluye conceptos que tienen al menos un registro.
     *
     * @param usuarioId ID del usuario
     * @return Lista de ConceptoResumenDTO
     */
    @Transactional(readOnly = true)
    public List<GastuApp.Conceptos.DTO.ConceptoResumenDTO> obtenerResumenConceptos(Long usuarioId) {
        // Obtener estadísticas crudas del repositorio: [conceptoId, cantidad, total]
        List<Object[]> estadisticas = movimientoRepository.contarYSumarEgresosPorConcepto(usuarioId);

        if (estadisticas.isEmpty()) {
            return List.of();
        }

        // Extraer IDs de conceptos
        List<Long> conceptoIds = estadisticas.stream()
                .map(obj -> (Long) obj[0])
                .collect(Collectors.toList());

        // Obtener mapa de conceptos para acceso rápido
        java.util.Map<Long, ConceptoDTO> conceptosMap = conceptoService.obtenerPorIds(conceptoIds).stream()
                .collect(Collectors.toMap(ConceptoDTO::getId, c -> c));

        return estadisticas.stream().map(obj -> {
            Long conceptoId = (Long) obj[0];
            Long cantidad = (Long) obj[1];
            java.math.BigDecimal total = (java.math.BigDecimal) obj[2];

            // Obtener detalles del concepto del mapa
            ConceptoDTO concepto = conceptosMap.get(conceptoId);

            // Fallback por si acaso
            if (concepto == null) {
                return null;
            }

            return new GastuApp.Conceptos.DTO.ConceptoResumenDTO(
                    concepto.getId(),
                    concepto.getNombre(),
                    concepto.getDescripcion(),
                    cantidad,
                    total);
        })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los egresos de un usuario filtrados por concepto.
     *
     * @param usuarioId  ID del usuario
     * @param conceptoId ID del concepto
     * @return Lista de egresos
     */
    @Transactional(readOnly = true)
    public List<EgresoDTO> obtenerEgresosPorConcepto(Long usuarioId, Long conceptoId) {
        List<Movimiento> egresos = movimientoRepository
                .findByUsuarioIdAndConceptoIdAndTipoOrderByFechaRegistroDesc(usuarioId, conceptoId,
                        TipoMovimiento.EGRESO);

        return egresos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Valida que un concepto exista y sea del tipo correcto.
     * Usa el ConceptoService local.
     *
     * @param conceptoId   ID del concepto a validar
     * @param tipoEsperado Tipo esperado del concepto (INGRESO o EGRESO)
     * @throws RuntimeException si el concepto no existe o no es del tipo correcto
     */
    private void validarConcepto(Long conceptoId, String tipoEsperado) {
        if (conceptoId == null) {
            throw new RuntimeException("El ID del concepto es requerido");
        }
        try {
            ConceptoDTO concepto = conceptoService.obtenerPorId(conceptoId);

            if (!tipoEsperado.equals(concepto.getTipo())) {
                throw new RuntimeException("El concepto seleccionado no es de tipo " + tipoEsperado);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al validar el concepto: " + e.getMessage());
        }
    }

    /**
     * Convierte una entidad Movimiento a un DTO de Egreso.
     *
     * @param movimiento Entidad a convertir
     * @return DTO con los datos del egreso
     */
    private EgresoDTO convertirADTO(Movimiento movimiento) {
        EgresoDTO dto = new EgresoDTO();
        dto.setId(movimiento.getId());
        dto.setMonto(movimiento.getMonto());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setConceptoId(movimiento.getConceptoId());
        dto.setFechaRegistro(movimiento.getFechaRegistro());
        return dto;
    }
}
