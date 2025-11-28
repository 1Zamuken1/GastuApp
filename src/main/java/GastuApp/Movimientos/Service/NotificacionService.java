package GastuApp.Movimientos.Service;

import GastuApp.Movimientos.DTO.NotificacionDTO;
import GastuApp.Movimientos.Entity.Notificacion;
import GastuApp.Movimientos.Entity.Notificacion.TipoNotificacion;
import GastuApp.Movimientos.Repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de notificaciones.
 * Maneja la creación, consulta y actualización de notificaciones de usuario.
 */
@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Crea una nueva notificación.
     *
     * @param usuarioId    ID del usuario destinatario
     * @param tipo         Tipo de notificación
     * @param referenciaId ID de referencia (opcional)
     * @param titulo       Título de la notificación
     * @param descripcion  Descripción de la notificación
     * @return DTO de la notificación creada
     */
    @Transactional
    public NotificacionDTO crearNotificacion(Long usuarioId, TipoNotificacion tipo,
            Long referenciaId, String titulo, String descripcion) {
        Notificacion notificacion = new Notificacion(usuarioId, tipo, referenciaId, titulo, descripcion);
        Notificacion notificacionGuardada = notificacionRepository.save(notificacion);
        return convertirADTO(notificacionGuardada);
    }

    /**
     * Obtiene todas las notificaciones de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de DTOs de notificaciones ordenadas por fecha descendente
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerNotificacionesPorUsuario(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);

        return notificaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las notificaciones no leídas de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de DTOs de notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerNotificacionesNoLeidas(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioIdAndLeidaOrderByFechaCreacionDesc(usuarioId, false);

        return notificaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Marca una notificación como leída.
     *
     * @param notificacionId ID de la notificación
     * @param usuarioId      ID del usuario (para validar permisos)
     * @return DTO de la notificación actualizada
     * @throws RuntimeException si la notificación no existe o no pertenece al
     *                          usuario
     */
    @Transactional
    public NotificacionDTO marcarComoLeida(Long notificacionId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        if (!notificacion.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tiene permisos para modificar esta notificación");
        }

        notificacion.setLeida(true);
        Notificacion notificacionActualizada = notificacionRepository.save(notificacion);

        return convertirADTO(notificacionActualizada);
    }

    /**
     * Cuenta las notificaciones no leídas de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Cantidad de notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public Long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeida(usuarioId, false);
    }

    /**
     * Convierte una entidad Notificacion a DTO.
     *
     * @param notificacion Entidad a convertir
     * @return DTO con los datos de la notificación
     */
    private NotificacionDTO convertirADTO(Notificacion notificacion) {
        return new NotificacionDTO(
                notificacion.getId(),
                notificacion.getTipo().name(),
                notificacion.getReferenciaId(),
                notificacion.getTitulo(),
                notificacion.getDescripcion(),
                notificacion.getLeida(),
                notificacion.getFechaCreacion());
    }
}
