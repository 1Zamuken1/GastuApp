package GastuApp.Movimientos.Repository;

import GastuApp.Movimientos.Entity.Movimiento;
import GastuApp.Movimientos.Entity.Movimiento.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceso a datos de la entidad Movimiento. Proporciona metodos
 * de consulta personalizados para filtrar movimientos
 * por usuario y tipo.
 */
@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByUsuarioIdAndTipoOrderByFechaRegistroDesc(Long usuarioId, TipoMovimiento tipo);

    Optional<Movimiento> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = 'INGRESO'")
    BigDecimal calcularTotalIngresos(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = 'EGRESO'")
    BigDecimal calcularTotalEgresos(@Param("usuarioId") Long usuarioId);

    Long countByUsuarioIdAndTipo(Long usuarioId, TipoMovimiento tipo);

    List<Movimiento> findByUsuarioIdAndConceptoIdAndTipoOrderByFechaRegistroDesc(Long usuarioId, Long conceptoId,
            TipoMovimiento tipo);

    @Query("SELECT m.conceptoId, COUNT(m), SUM(m.monto) FROM Movimiento m WHERE m.usuarioId = :uid AND m.tipo = 'INGRESO' GROUP BY m.conceptoId")
    List<Object[]> contarYSumarIngresosPorConcepto(@Param("uid") Long usuarioId);

    @Query("SELECT m.conceptoId, COUNT(m), SUM(m.monto) FROM Movimiento m WHERE m.usuarioId = :uid AND m.tipo = 'EGRESO' GROUP BY m.conceptoId")
    List<Object[]> contarYSumarEgresosPorConcepto(@Param("uid") Long usuarioId);

    // ==================== QUERIES PARA ANÁLISIS HISTÓRICO DE ALERTAS
    // ====================

    @Query("SELECT m FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo AND m.fechaRegistro BETWEEN :fechaDesde AND :fechaHasta")
    List<Movimiento> findByUsuarioIdAndTipoAndFechaRegistroBetween(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo AND m.fechaRegistro BETWEEN :fechaDesde AND :fechaHasta")
    BigDecimal calcularTotalEnRango(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta);

    Long countByUsuarioIdAndTipoAndFechaRegistroBetween(
            Long usuarioId,
            TipoMovimiento tipo,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta);

    List<Movimiento> findByUsuarioIdAndTipoAndFechaRegistroAfter(
            Long usuarioId,
            TipoMovimiento tipo,
            LocalDateTime fechaDesde);

    @Query("SELECT m FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo ORDER BY m.fechaRegistro DESC LIMIT 1")
    Optional<Movimiento> findUltimoMovimiento(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoMovimiento tipo);

    @Query("SELECT COUNT(m) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo AND m.monto <= :montoMax AND m.fechaRegistro BETWEEN :fechaDesde AND :fechaHasta")
    Long countMicroMovimientos(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("montoMax") BigDecimal montoMax,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta);

    @Query("SELECT m.conceptoId, COUNT(m), SUM(m.monto) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo AND m.fechaRegistro BETWEEN :fechaDesde AND :fechaHasta GROUP BY m.conceptoId")
    List<Object[]> obtenerEstadisticasPorConceptoEnRango(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta);

    @Query("SELECT COUNT(m) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo AND m.conceptoId IS NULL")
    Long countMovimientosSinConcepto(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoMovimiento tipo);

    /**
     * Calcula el total de movimientos del mes actual.
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Movimiento m WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo AND YEAR(m.fechaRegistro) = YEAR(CURRENT_DATE) AND MONTH(m.fechaRegistro) = MONTH(CURRENT_DATE)")
    BigDecimal calcularTotalMesActual(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoMovimiento tipo);
}