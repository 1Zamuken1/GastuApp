package GastuApp.Movimientos.Service;

import GastuApp.Conceptos.DTO.ConceptoDTO;
import GastuApp.Movimientos.DTO.IngresoDTO;
import GastuApp.Movimientos.Entity.Movimiento;
import GastuApp.Movimientos.Entity.Movimiento.TipoMovimiento;
import GastuApp.Movimientos.Repository.MovimientoRepository;
import GastuApp.Conceptos.Service.ConceptoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que contiene la lógica de negocio para la gestión de Ingresos.
 */
@Service
public class IngresoService {

    private final MovimientoRepository movimientoRepository;
    private final ConceptoService conceptoService;

    public IngresoService(MovimientoRepository movimientoRepository, ConceptoService conceptoService) {
        this.movimientoRepository = movimientoRepository;
        this.conceptoService = conceptoService;
    }

    /**
     * Obtiene todos los ingresos de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return Lista de DTOs de ingresos ordenados por fecha descendente
     */
    @Transactional(readOnly = true)
    public List<IngresoDTO> obtenerIngresosPorUsuario(Long usuarioId) {
        List<Movimiento> ingresos = movimientoRepository
                .findByUsuarioIdAndTipoOrderByFechaRegistroDesc(usuarioId, TipoMovimiento.INGRESO);

        return ingresos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un ingreso específico por su ID.
     * Valida que el ingreso pertenezca al usuario solicitante.
     *
     * @param id        ID del ingreso
     * @param usuarioId ID del usuario solicitante
     * @return DTO del ingreso
     * @throws RuntimeException si el ingreso no existe o no pertenece al usuario
     */
    @Transactional(readOnly = true)
    public IngresoDTO obtenerIngresoPorId(Long id, Long usuarioId) {
        Movimiento ingreso = movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado o no tiene permisos para acceder"));

        if (ingreso.getTipo() != TipoMovimiento.INGRESO) {
            throw new RuntimeException("El movimiento solicitado no es un ingreso");
        }

        return convertirADTO(ingreso);
    }

    /**
     * Crea un nuevo ingreso para un usuario.
     * Valida que el concepto seleccionado exista y sea de tipo INGRESO.
     *
     * @param dto       DTO con los datos del ingreso
     * @param usuarioId ID del usuario que crea el ingreso
     * @return DTO del ingreso creado
     * @throws RuntimeException si el concepto no existe o no es válido
     */
    @Transactional
    public IngresoDTO crearIngreso(IngresoDTO dto, Long usuarioId) {
        // Validar que el concepto existe y es de tipo INGRESO
        validarConcepto(dto.getConceptoId(), "INGRESO");

        // Crear entidad
        Movimiento ingreso = new Movimiento();
        ingreso.setTipo(TipoMovimiento.INGRESO);
        ingreso.setMonto(dto.getMonto());
        ingreso.setDescripcion(dto.getDescripcion());
        ingreso.setUsuarioId(usuarioId);
        ingreso.setConceptoId(dto.getConceptoId());

        // Guardar
        Movimiento ingresoGuardado = movimientoRepository.save(ingreso);

        return convertirADTO(ingresoGuardado);
    }

    /**
     * Actualiza un ingreso existente.
     * Valida que el ingreso pertenezca al usuario y que el concepto sea válido.
     *
     * @param id        ID del ingreso a actualizar
     * @param dto       DTO con los nuevos datos
     * @param usuarioId ID del usuario que actualiza
     * @return DTO del ingreso actualizado
     * @throws RuntimeException si el ingreso no existe, no pertenece al usuario o
     *                          el concepto no es válido
     */
    @Transactional
    public IngresoDTO actualizarIngreso(Long id, IngresoDTO dto, Long usuarioId) {
        // Buscar ingreso existente
        Movimiento ingreso = movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado o no tiene permisos para modificarlo"));

        if (ingreso.getTipo() != TipoMovimiento.INGRESO) {
            throw new RuntimeException("El movimiento no es un ingreso");
        }

        // Validar nuevo concepto si cambió
        if (!ingreso.getConceptoId().equals(dto.getConceptoId())) {
            validarConcepto(dto.getConceptoId(), "INGRESO");
        }

        // Actualizar campos
        ingreso.setMonto(dto.getMonto());
        ingreso.setDescripcion(dto.getDescripcion());
        ingreso.setConceptoId(dto.getConceptoId());

        // Guardar cambios
        Movimiento ingresoActualizado = movimientoRepository.save(ingreso);

        return convertirADTO(ingresoActualizado);
    }

    /**
     * Elimina un ingreso.
     * Valida que el ingreso pertenezca al usuario solicitante.
     *
     * @param id        ID del ingreso a eliminar
     * @param usuarioId ID del usuario que solicita la eliminación
     * @throws RuntimeException si el ingreso no existe o no pertenece al usuario
     */
    @Transactional
    public void eliminarIngreso(Long id, Long usuarioId) {
        Movimiento ingreso = movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado o no tiene permisos para eliminarlo"));

        if (ingreso.getTipo() != TipoMovimiento.INGRESO) {
            throw new RuntimeException("El movimiento no es un ingreso");
        }

        movimientoRepository.delete(ingreso);
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
     * Obtiene un resumen de conceptos de INGRESO con estadísticas (cantidad y
     * total).
     * Solo incluye conceptos que tienen al menos un registro.
     *
     * @param usuarioId ID del usuario
     * @return Lista de ConceptoResumenDTO
     */
    @Transactional(readOnly = true)
    public List<GastuApp.Conceptos.DTO.ConceptoResumenDTO> obtenerResumenConceptos(Long usuarioId) {
        // Obtener estadísticas crudas del repositorio: [conceptoId, cantidad, total]
        List<Object[]> estadisticas = movimientoRepository.contarYSumarIngresosPorConcepto(usuarioId);

        if (estadisticas.isEmpty()) {
            return List.of();
        }

        // Extraer IDs de conceptos para hacer una sola consulta (batch fetch, evita
        // N+1)
        List<Long> conceptoIds = estadisticas.stream()
                .map(obj -> (Long) obj[0])
                .collect(Collectors.toList());

        // Obtener mapa de conceptos para acceso rápido O(1)
        java.util.Map<Long, ConceptoDTO> conceptosMap = conceptoService.obtenerPorIds(conceptoIds).stream()
                .collect(Collectors.toMap(ConceptoDTO::getId, c -> c));

        return estadisticas.stream().map(obj -> {
            Long conceptoId = (Long) obj[0];
            Long cantidad = (Long) obj[1];
            java.math.BigDecimal total = (java.math.BigDecimal) obj[2];

            // Obtener detalles del concepto del mapa
            ConceptoDTO concepto = conceptosMap.get(conceptoId);

            // Fallback por si acaso (aunque no debería pasar si la integridad referencial
            // está bien)
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
     * Obtiene los ingresos de un usuario filtrados por concepto.
     *
     * @param usuarioId  ID del usuario
     * @param conceptoId ID del concepto
     * @return Lista de ingresos
     */
    @Transactional(readOnly = true)
    public List<IngresoDTO> obtenerIngresosPorConcepto(Long usuarioId, Long conceptoId) {
        List<Movimiento> ingresos = movimientoRepository
                .findByUsuarioIdAndConceptoIdAndTipoOrderByFechaRegistroDesc(usuarioId, conceptoId,
                        TipoMovimiento.INGRESO);

        return ingresos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Movimiento a un DTO de Ingreso.
     *
     * @param movimiento Entidad a convertir
     * @return DTO con los datos del ingreso
     */
    private IngresoDTO convertirADTO(Movimiento movimiento) {
        IngresoDTO dto = new IngresoDTO();
        dto.setId(movimiento.getId());
        dto.setMonto(movimiento.getMonto());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setConceptoId(movimiento.getConceptoId());
        dto.setFechaRegistro(movimiento.getFechaRegistro());
        return dto;
    }
}