package GastuApp.Movimientos.Service;

import GastuApp.Movimientos.DTO.ConceptoDTO;
import GastuApp.Movimientos.DTO.IngresoDTO;
import GastuApp.Movimientos.Entity.Movimiento;
import GastuApp.Movimientos.Entity.Movimiento.TipoMovimiento;
import GastuApp.Movimientos.Repository.MovimientoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que contiene la logica de negocio para la gestion de Ingresos.
 * Maneja operaciones CRUD y validaciones necesarias.
 */
@Service
public class IngresoService {

    private final MovimientoRepository movimientoRepository;
    private final RestTemplate restTemplate;

    @Value("${usuarios.service.url}")
    private String usuariosServiceUrl;

    public IngresoService(MovimientoRepository movimientoRepository, RestTemplate restTemplate) {
        this.movimientoRepository = movimientoRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene todos los ingresos de un usuario especifico.
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
     * Obtiene un ingreso especifico por su ID.
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
     * @param token     Token JWT para validar el concepto
     * @return DTO del ingreso creado
     * @throws RuntimeException si el concepto no existe o no es valido
     */
    @Transactional
    public IngresoDTO crearIngreso(IngresoDTO dto, Long usuarioId, String token) {
        // Validar que el concepto existe y es de tipo INGRESO
        validarConcepto(dto.getConceptoId(), "INGRESO", token);

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
     * Valida que el ingreso pertenezca al usuario y que el concepto sea valido.
     *
     * @param id        ID del ingreso a actualizar
     * @param dto       DTO con los nuevos datos
     * @param usuarioId ID del usuario que actualiza
     * @param token     Token JWT para validar el concepto
     * @return DTO del ingreso actualizado
     * @throws RuntimeException si el ingreso no existe, no pertenece al usuario o
     *                          el concepto no es valido
     */
    @Transactional
    public IngresoDTO actualizarIngreso(Long id, IngresoDTO dto, Long usuarioId, String token) {
        // Buscar ingreso existente
        Movimiento ingreso = movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado o no tiene permisos para modificarlo"));

        if (ingreso.getTipo() != TipoMovimiento.INGRESO) {
            throw new RuntimeException("El movimiento no es un ingreso");
        }

        // Validar nuevo concepto si cambio
        if (!ingreso.getConceptoId().equals(dto.getConceptoId())) {
            validarConcepto(dto.getConceptoId(), "INGRESO", token);
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
     * @param usuarioId ID del usuario que solicita la eliminacion
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
     * Realiza una llamada al microservicio de usuarios para verificar el concepto.
     *
     * @param conceptoId   ID del concepto a validar
     * @param tipoEsperado Tipo esperado del concepto (INGRESO o EGRESO)
     * @param token        Token JWT para autenticacion
     * @throws RuntimeException si el concepto no existe o no es del tipo correcto
     */
    private void validarConcepto(Long conceptoId, String tipoEsperado, String token) {
        try {
            String url = usuariosServiceUrl + "/api/conceptos/" + conceptoId;

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            if (token != null && !token.isEmpty()) {
                // Asegurarse de que el token tenga el prefijo Bearer si no lo tiene
                if (!token.startsWith("Bearer ")) {
                    token = "Bearer " + token;
                }
                headers.set("Authorization", token);
            }

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<ConceptoDTO> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    ConceptoDTO.class);

            ConceptoDTO concepto = response.getBody();

            if (concepto == null) {
                throw new RuntimeException("Concepto no encontrado");
            }

            if (!tipoEsperado.equals(concepto.getTipo())) {
                throw new RuntimeException("El concepto seleccionado no es de tipo " + tipoEsperado);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al validar el concepto: " + e.getMessage());
        }
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