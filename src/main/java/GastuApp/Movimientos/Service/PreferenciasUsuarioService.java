package GastuApp.Movimientos.Service;

import GastuApp.Movimientos.DTO.PreferenciasFinancierasDTO;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PreferenciasUsuarioService {

    /**
     * Almacenamiento en memoria de preferencias por usuario.
     * Key: usuarioId, Value: PreferenciasFinancieras
     */
    private final ConcurrentHashMap<Long, PreferenciasFinancieras> preferenciasMap = new ConcurrentHashMap<>();

    /**
     * Valores por defecto para las preferencias.
     */
    private static final int UMBRAL_ADVERTENCIA_DEFAULT = 80;
    private static final int EGRESO_GRANDE_DEFAULT = 30;
    private static final boolean ALERTA_EGRESO_GRANDE_DEFAULT = true;

    /**
     * Obtiene las preferencias de un usuario.
     * Si el usuario no tiene preferencias configuradas, retorna valores por
     * defecto.
     *
     * @param usuarioId ID del usuario
     * @return DTO con las preferencias del usuario
     */
    public PreferenciasFinancierasDTO obtenerPreferencias(Long usuarioId) {
        PreferenciasFinancieras preferencias = preferenciasMap.getOrDefault(
                usuarioId,
                crearPreferenciasDefault());

        return convertirADTO(preferencias);
    }

    /**
     * Actualiza las preferencias de un usuario.
     *
     * @param usuarioId ID del usuario
     * @param dto       DTO con las nuevas preferencias
     * @return DTO con las preferencias actualizadas
     */
    public PreferenciasFinancierasDTO actualizarPreferencias(Long usuarioId, PreferenciasFinancierasDTO dto) {
        PreferenciasFinancieras preferencias = new PreferenciasFinancieras(
                dto.getUmbralAdvertenciaPorcentaje(),
                dto.getEgresoGrandePorcentaje(),
                dto.getAlertaEgresoGrandeActiva());

        preferenciasMap.put(usuarioId, preferencias);

        return convertirADTO(preferencias);
    }

    /**
     * Resetea las preferencias de un usuario a los valores por defecto.
     *
     * @param usuarioId ID del usuario
     * @return DTO con las preferencias por defecto
     */
    public PreferenciasFinancierasDTO resetearPreferencias(Long usuarioId) {
        PreferenciasFinancieras preferencias = crearPreferenciasDefault();
        preferenciasMap.put(usuarioId, preferencias);
        return convertirADTO(preferencias);
    }

    /**
     * Crea una instancia de preferencias con valores por defecto.
     *
     * @return PreferenciasFinancieras con valores por defecto
     */
    private PreferenciasFinancieras crearPreferenciasDefault() {
        return new PreferenciasFinancieras(
                UMBRAL_ADVERTENCIA_DEFAULT,
                EGRESO_GRANDE_DEFAULT,
                ALERTA_EGRESO_GRANDE_DEFAULT);
    }

    /**
     * Convierte una entidad de preferencias a DTO.
     *
     * @param preferencias Entidad de preferencias
     * @return DTO de preferencias
     */
    private PreferenciasFinancierasDTO convertirADTO(PreferenciasFinancieras preferencias) {
        return new PreferenciasFinancierasDTO(
                preferencias.umbralAdvertenciaPorcentaje,
                preferencias.egresoGrandePorcentaje,
                preferencias.alertaEgresoGrandeActiva);
    }

    /**
     * Clase interna para almacenar preferencias financieras.
     * Inmutable para thread-safety.
     */
    private static class PreferenciasFinancieras {
        private final int umbralAdvertenciaPorcentaje;
        private final int egresoGrandePorcentaje;
        private final boolean alertaEgresoGrandeActiva;

        public PreferenciasFinancieras(int umbralAdvertenciaPorcentaje,
                int egresoGrandePorcentaje,
                boolean alertaEgresoGrandeActiva) {
            this.umbralAdvertenciaPorcentaje = umbralAdvertenciaPorcentaje;
            this.egresoGrandePorcentaje = egresoGrandePorcentaje;
            this.alertaEgresoGrandeActiva = alertaEgresoGrandeActiva;
        }
    }
}
