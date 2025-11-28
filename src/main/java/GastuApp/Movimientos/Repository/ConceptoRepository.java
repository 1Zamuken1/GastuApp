package GastuApp.Movimientos.Repository;

import GastuApp.Movimientos.Entity.Concepto;
import GastuApp.Movimientos.Entity.Concepto.TipoConcepto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceso a datos de la entidad Concepto.
 * Proporciona métodos de consulta personalizados para gestionar conceptos.
 */
@Repository
public interface ConceptoRepository extends JpaRepository<Concepto, Long> {

    /**
     * Busca todos los conceptos de un tipo específico.
     *
     * @param tipo Tipo de concepto (INGRESO, EGRESO, AHORRO)
     * @return Lista de conceptos del tipo especificado
     */
    List<Concepto> findByTipo(TipoConcepto tipo);

    /**
     * Busca conceptos por nombre (búsqueda parcial, case-insensitive).
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de conceptos que coinciden con el nombre
     */
    List<Concepto> findByNombreContainingIgnoreCase(String nombre);
}
