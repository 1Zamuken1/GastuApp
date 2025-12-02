package GastuApp.Movimientos.Repository;

import GastuApp.Movimientos.Entity.PreferenciasAlertas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar las preferencias de alertas de usuario.
 */
@Repository
public interface PreferenciasAlertasRepository extends JpaRepository<PreferenciasAlertas, Long> {

    /**
     * Busca las preferencias de un usuario específico.
     * 
     * @param usuarioId ID del usuario
     * @return Optional con las preferencias si existen
     */
    Optional<PreferenciasAlertas> findByUsuarioId(Long usuarioId);

    /**
     * Verifica si un usuario ya tiene preferencias configuradas.
     * 
     * @param usuarioId ID del usuario
     * @return true si existen preferencias, false en caso contrario
     */
    boolean existsByUsuarioId(Long usuarioId);
}
