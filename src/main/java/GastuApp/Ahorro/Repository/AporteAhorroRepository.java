package GastuApp.Ahorro.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import GastuApp.Ahorro.Entity.AporteAhorro;
import java.time.LocalDate;

@Repository
public interface AporteAhorroRepository extends JpaRepository<AporteAhorro, Long> {

        // ver todas las cuotas de una meta ordenadas por fecha límite
        List<AporteAhorro> findByMetaIdOrderByFechaLimiteAsc(Long metaId);

        // buscar cuota por id asegurando que pertenece a la meta
        Optional<AporteAhorro> findByAporteAhorroIdAndMetaId(Long aporteAhorroId, Long metaId);

        // Saber cuántos aportes tiene una meta
        Long countByMetaId(Long metaId);

        // Sumar aportes realizados (estado APORTADO) por usuario y rango de fechas
        // (usando fechaLimite como proxy)
        @Query(value = "SELECT COALESCE(SUM(aa.aporte), 0) FROM aporte_ahorro aa " +
                        "INNER JOIN ahorro_meta am ON aa.ahorro_meta_id = am.ahorro_meta_id " +
                        "WHERE am.usuario_id = :usuarioId " +
                        "AND aa.estado = 'APORTADO' " +
                        "AND aa.fecha_limite BETWEEN :fechaInicio AND :fechaFin", nativeQuery = true)
        java.math.BigDecimal sumarAportesPorUsuarioYRango(
                        @Param("usuarioId") Long usuarioId,
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        // Optimización Dashboard: Totales diarios de ahorro
        @Query(value = "SELECT DAY(aa.fecha_limite) as dia, COALESCE(SUM(aa.aporte), 0) as total " +
                        "FROM aporte_ahorro aa " +
                        "INNER JOIN ahorro_meta am ON aa.ahorro_meta_id = am.ahorro_meta_id " +
                        "WHERE am.usuario_id = :usuarioId " +
                        "AND aa.estado = 'APORTADO' " +
                        "AND aa.fecha_limite BETWEEN :fechaInicio AND :fechaFin " +
                        "GROUP BY DAY(aa.fecha_limite) " +
                        "ORDER BY dia", nativeQuery = true)
        List<Object[]> obtenerAportesDiariosPorUsuarioYRango(
                        @Param("usuarioId") Long usuarioId,
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

}
