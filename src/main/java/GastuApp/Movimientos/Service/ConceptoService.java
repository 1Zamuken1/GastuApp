package GastuApp.Movimientos.Service;

import GastuApp.Movimientos.DTO.ConceptoDTO;
import GastuApp.Movimientos.Entity.Concepto;
import GastuApp.Movimientos.Entity.Concepto.TipoConcepto;
import GastuApp.Movimientos.Repository.ConceptoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de conceptos.
 * Maneja operaciones CRUD y validaciones de conceptos.
 */
@Service
public class ConceptoService {

    private final ConceptoRepository conceptoRepository;

    public ConceptoService(ConceptoRepository conceptoRepository) {
        this.conceptoRepository = conceptoRepository;
    }

    /**
     * Obtiene todos los conceptos.
     *
     * @return Lista de todos los conceptos
     */
    @Transactional(readOnly = true)
    public List<ConceptoDTO> obtenerTodos() {
        return conceptoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene conceptos filtrados por tipo.
     *
     * @param tipo Tipo de concepto (INGRESO, EGRESO, AHORRO)
     * @return Lista de conceptos del tipo especificado
     */
    @Transactional(readOnly = true)
    public List<ConceptoDTO> obtenerPorTipo(String tipo) {
        TipoConcepto tipoConcepto = TipoConcepto.valueOf(tipo.toUpperCase());
        return conceptoRepository.findByTipo(tipoConcepto).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un concepto por su ID.
     *
     * @param id ID del concepto
     * @return DTO del concepto
     * @throws RuntimeException si el concepto no existe
     */
    @Transactional(readOnly = true)
    public ConceptoDTO obtenerPorId(Long id) {
        Concepto concepto = conceptoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Concepto no encontrado"));
        return convertirADTO(concepto);
    }

    /**
     * Crea un nuevo concepto.
     * Solo usuarios con rol ADMIN pueden crear conceptos.
     *
     * @param dto DTO con los datos del concepto
     * @return DTO del concepto creado
     */
    @Transactional
    public ConceptoDTO crearConcepto(ConceptoDTO dto) {
        // Validar que el tipo sea válido
        TipoConcepto tipo;
        try {
            tipo = TipoConcepto.valueOf(dto.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de concepto inválido. Debe ser: INGRESO, EGRESO o AHORRO");
        }

        Concepto concepto = new Concepto();
        concepto.setTipo(tipo);
        concepto.setNombre(dto.getNombre());
        concepto.setDescripcion(dto.getDescripcion());

        Concepto conceptoGuardado = conceptoRepository.save(concepto);
        return convertirADTO(conceptoGuardado);
    }

    /**
     * Actualiza un concepto existente.
     * Solo usuarios con rol ADMIN pueden actualizar conceptos.
     *
     * @param id  ID del concepto a actualizar
     * @param dto DTO con los nuevos datos
     * @return DTO del concepto actualizado
     */
    @Transactional
    public ConceptoDTO actualizarConcepto(Long id, ConceptoDTO dto) {
        Concepto concepto = conceptoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Concepto no encontrado"));

        // Validar que el tipo sea válido
        TipoConcepto tipo;
        try {
            tipo = TipoConcepto.valueOf(dto.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de concepto inválido. Debe ser: INGRESO, EGRESO o AHORRO");
        }

        concepto.setTipo(tipo);
        concepto.setNombre(dto.getNombre());
        concepto.setDescripcion(dto.getDescripcion());

        Concepto conceptoActualizado = conceptoRepository.save(concepto);
        return convertirADTO(conceptoActualizado);
    }

    /**
     * Elimina un concepto.
     * Solo usuarios con rol ADMIN pueden eliminar conceptos.
     * NOTA: La BD tiene ON DELETE SET NULL, por lo que los movimientos
     * asociados no se eliminarán, solo se desvinculará el concepto.
     *
     * @param id ID del concepto a eliminar
     */
    @Transactional
    public void eliminarConcepto(Long id) {
        if (!conceptoRepository.existsById(id)) {
            throw new RuntimeException("Concepto no encontrado");
        }
        conceptoRepository.deleteById(id);
    }

    /**
     * Convierte una entidad Concepto a DTO.
     *
     * @param concepto Entidad a convertir
     * @return DTO del concepto
     */
    private ConceptoDTO convertirADTO(Concepto concepto) {
        return new ConceptoDTO(
                concepto.getId(),
                concepto.getTipo().name(),
                concepto.getNombre(),
                concepto.getDescripcion());
    }
}
