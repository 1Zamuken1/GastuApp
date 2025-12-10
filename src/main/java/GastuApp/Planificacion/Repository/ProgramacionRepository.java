package GastuApp.Planificacion.Repository;

import GastuApp.Planificacion.Entities.Programacion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProgramacionRepository extends JpaRepository<Programacion, Long> {

    /**
     * Obtiene todas las programaciones de un usuario.
     */
    List<Programacion> findByUsuarioId(Long usuarioId);


    /**
     * Obtiene las programaciones que deben ejecutarse hoy (para alertas).
     */
    List<Programacion> findByUsuarioIdAndProximaEjecucion(Long usuarioId, LocalDate fecha);


    /**
     * Búsqueda por término (search bar) para filtrar programaciones.
     * Busca por descripción, frecuencia o monto.
     */
    @Query("""
        SELECT p FROM Programacion p 
        WHERE p.usuarioId = :usuarioId
        AND (
            LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :termino, '%'))
            OR LOWER(p.frecuencia) LIKE LOWER(CONCAT('%', :termino, '%'))
            OR CAST(p.montoProgramado AS string) LIKE CONCAT('%', :termino, '%')
        )
    """)
    List<Programacion> search(
            @Param("usuarioId") Long usuarioId,
            @Param("termino") String termino
    );

    
}
