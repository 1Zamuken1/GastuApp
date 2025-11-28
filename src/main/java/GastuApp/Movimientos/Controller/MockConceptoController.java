package GastuApp.Movimientos.Controller;

import GastuApp.Movimientos.DTO.ConceptoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador Mock para simular el servicio de Conceptos.
 * NECESARIO porque el código real de Conceptos no está presente en este
 * proyecto.
 * Permite que el servicio de Ingresos valide conceptos sin fallar.
 */
@RestController
@RequestMapping("/api/conceptos")
public class MockConceptoController {

    @GetMapping("/{id}")
    public ResponseEntity<ConceptoDTO> obtenerConcepto(@PathVariable Long id) {
        // Retornar siempre un concepto válido de tipo INGRESO para pruebas
        ConceptoDTO mockConcepto = new ConceptoDTO(id, "INGRESO", "Concepto Mock", "Concepto simulado para pruebas");
        return ResponseEntity.ok(mockConcepto);
    }
}
