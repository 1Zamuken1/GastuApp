package GastuApp.AgenteFinanciero.repository;

import GastuApp.AgenteFinanciero.entity.ConsultaIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaIARepository extends JpaRepository<ConsultaIA, Long> {
    
    List<ConsultaIA> findByUsuarioIdOrderByFechaConsultaDesc(Long usuarioId);
    
    @Query("SELECT c FROM ConsultaIA c WHERE c.usuarioId = :usuarioId " +
           "AND c.fechaConsulta >= :fechaInicio ORDER BY c.fechaConsulta DESC")
    List<ConsultaIA> findConsultasRecientes(
        @Param("usuarioId") Long usuarioId,
        @Param("fechaInicio") LocalDateTime fechaInicio
    );
    
    @Query("SELECT COUNT(c) FROM ConsultaIA c WHERE c.usuarioId = :usuarioId " +
           "AND c.fechaConsulta >= :fechaInicio")
    Long contarConsultasDelDia(
        @Param("usuarioId") Long usuarioId,
        @Param("fechaInicio") LocalDateTime fechaInicio
    );
}