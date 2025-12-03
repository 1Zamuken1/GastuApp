package GastuApp.Planificacion.Repository;

import GastuApp.Planificacion.Entities.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    // Buscar por usuario 
    List<Presupuesto> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    // Buscar por id y usuario 
    java.util.Optional<Presupuesto> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query(
        value = "SELECT * FROM presupuesto p " +
                "WHERE p.usuario_id = :usuarioId AND (" +
                "   CONCAT_WS(' ', p.presupuesto_id, p.limite, p.fecha_inicio, p.fecha_fin, p.activo, p.concepto_id) " +
                "   LIKE %:value%" +
                ") " +
                "ORDER BY p.fecha_creacion DESC",
        nativeQuery = true
    )
    List<Presupuesto> searchByUsuarioIdAndValue(@Param("usuarioId") Long usuarioId, @Param("value") String value);
}
