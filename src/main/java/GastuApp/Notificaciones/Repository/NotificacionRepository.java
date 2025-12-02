package GastuApp.Notificaciones.Repository;

import GastuApp.Notificaciones.Entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceso a datos de la entidad Notificacion.
 * Proporciona métodos de consulta personalizados para gestionar notificaciones.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Busca todas las notificaciones de un usuario ordenadas por fecha descendente.
     *
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones ordenadas por fecha de creación descendente
     */
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    /**
     * Busca notificaciones de un usuario filtradas por estado de lectura.
     *
     * @param usuarioId ID del usuario
     * @param leida     Estado de lectura (true = leídas, false = no leídas)
     * @return Lista de notificaciones filtradas y ordenadas por fecha descendente
     */
    List<Notificacion> findByUsuarioIdAndLeidaOrderByFechaCreacionDesc(Long usuarioId, Boolean leida);

    /**
     * Cuenta las notificaciones de un usuario por estado de lectura.
     *
     * @param usuarioId ID del usuario
     * @param leida     Estado de lectura
     * @return Cantidad de notificaciones
     */
    Long countByUsuarioIdAndLeida(Long usuarioId, Boolean leida);
}
