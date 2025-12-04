package GastuApp.Ahorro.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import GastuApp.Ahorro.Entity.AporteAhorro;

@Repository
public interface AporteAhorroRepository extends JpaRepository<AporteAhorro, Long> {

// ver todas las cuotas de una meta ordenadas por fecha límite
    List<AporteAhorro> findByMetaIdOrderByFechaLimiteAsc(Long metaId);

//buscar cuota por id asegurando que pertenece a la meta
    Optional<AporteAhorro> findByAporteAhorroIdAndMetaId(Long aporteAhorroId, Long metaId);

// Saber cuántos aportes tiene una meta
    Long countByMetaId(Long metaId);

}
