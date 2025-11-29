package GastuApp.Movimientos.Repository;

import GastuApp.Movimientos.Entity.Movimiento;
import GastuApp.Movimientos.Entity.Movimiento.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceso a datos de la entidad Movimiento.
 * Proporciona metodos de consulta personalizados para filtrar movimientos
 * por usuario y tipo.
 */
@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /**
     * Busca todos los movimientos de un usuario especifico filtrados por tipo.
     *
     * @param usuarioId ID del usuario propietario
     * @param tipo      Tipo de movimiento (INGRESO o EGRESO)
     * @return Lista de movimientos ordenados por fecha descendente
     */
    List<Movimiento> findByUsuarioIdAndTipoOrderByFechaRegistroDesc(Long usuarioId, TipoMovimiento tipo);

    /**
     * Busca un movimiento especifico por ID y usuario.
     * Garantiza que un usuario solo pueda acceder a sus propios movimientos.
     *
     * @param id        ID del movimiento
     * @param usuarioId ID del usuario propietario
     * @return Optional con el movimiento si existe y pertenece al usuario
     */
    Optional<Movimiento> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Calcula el total de ingresos de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Suma total de todos los ingresos del usuario
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = 'INGRESO'")
    BigDecimal calcularTotalIngresos(@Param("usuarioId") Long usuarioId);

    /**
     * Calcula el total de egresos de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Suma total de todos los egresos del usuario
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = 'EGRESO'")
    BigDecimal calcularTotalEgresos(@Param("usuarioId") Long usuarioId);

    /**
     * Cuenta la cantidad de movimientos de un usuario por tipo.
     *
     * @param usuarioId ID del usuario
     * @param tipo      Tipo de movimiento
     * @return Cantidad de movimientos
     */
    Long countByUsuarioIdAndTipo(Long usuarioId, TipoMovimiento tipo);

    /**
     * Busca ingresos de un usuario filtrados por concepto.
     */
    List<Movimiento> findByUsuarioIdAndConceptoIdAndTipoOrderByFechaRegistroDesc(Long usuarioId, Long conceptoId,
            TipoMovimiento tipo);

    /**
     * Cuenta y suma ingresos por concepto para un usuario.
     * Retorna: [conceptoId, cantidad, total]
     */
    @Query("SELECT m.conceptoId, COUNT(m), SUM(m.monto) FROM Movimiento m WHERE m.usuarioId = :uid AND m.tipo = 'INGRESO' GROUP BY m.conceptoId")
    List<Object[]> contarYSumarIngresosPorConcepto(@Param("uid") Long usuarioId);

    /**
     * Cuenta y suma egresos por concepto para un usuario.
     * Retorna: [conceptoId, cantidad, total]
     */
    @Query("SELECT m.conceptoId, COUNT(m), SUM(m.monto) FROM Movimiento m WHERE m.usuarioId = :uid AND m.tipo = 'EGRESO' GROUP BY m.conceptoId")
    List<Object[]> contarYSumarEgresosPorConcepto(@Param("uid") Long usuarioId);
}