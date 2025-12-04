package GastuApp.Ahorro.Repository;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import GastuApp.Ahorro.Entity.AhorroMeta;
import GastuApp.Ahorro.Entity.AhorroMeta.Estado;

@Repository
public interface AhorroMetaRepository extends JpaRepository<AhorroMeta, Long>{

// lista todos los ahorros del usuario
    List<AhorroMeta> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

// Buscar un ahorro por id asegurando que pertenece al usuario
    Optional<AhorroMeta> findByAhorroMetaIdAndUsuarioId(Long ahorroId, Long usuarioId);

// filtra por estado
    List<AhorroMeta> findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(
            Long usuarioId,
            Estado estado
    );

 // Buscar un ahorro por el concepto id asegurando que pertenece al usuario
    List<AhorroMeta> findByUsuarioIdAndConceptoIdOrderByFechaCreacionDesc(
            Long usuarioId,
            Long conceptoId
    );

 // busca por el nombre parcial del concepto
@Query("SELECT a FROM AhorroMeta a " +
       "JOIN Concepto c ON a.conceptoId = c.conceptoId " +
       "WHERE a.usuarioId = :usuarioId " +
       "AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) " +
       "ORDER BY a.fechaCreacion DESC")
List<AhorroMeta> buscarPorConceptoParcial(
        @Param("usuarioId") Long usuarioId,
        @Param("texto") String texto
);
    
}
