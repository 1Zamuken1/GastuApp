package GastuApp.Movimientos.Service;

import GastuApp.Movimientos.DTO.PreferenciasFinancierasDTO;
import GastuApp.Movimientos.Entity.PreferenciasAlertas;
import GastuApp.Movimientos.Repository.PreferenciasAlertasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenciasUsuarioService {

    private final PreferenciasAlertasRepository preferenciasRepository;

    public PreferenciasUsuarioService(PreferenciasAlertasRepository preferenciasRepository) {
        this.preferenciasRepository = preferenciasRepository;
    }

    /**
     * Obtiene las preferencias de un usuario.
     * Si el usuario no tiene preferencias configuradas, retorna valores por
     * defecto.
     *
     * @param usuarioId ID del usuario
     * @return DTO con las preferencias del usuario
     */
    @Transactional(readOnly = true)
    public PreferenciasFinancierasDTO obtenerPreferencias(Long usuarioId) {
        PreferenciasAlertas preferencias = preferenciasRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearPreferenciasDefault(usuarioId));

        return convertirADTO(preferencias);
    }

    /**
     * Actualiza las preferencias de un usuario.
     *
     * @param usuarioId ID del usuario
     * @param dto       DTO con las nuevas preferencias
     * @return DTO con las preferencias actualizadas
     */
    @Transactional
    public PreferenciasFinancierasDTO actualizarPreferencias(Long usuarioId, PreferenciasFinancierasDTO dto) {
        PreferenciasAlertas preferencias = preferenciasRepository.findByUsuarioId(usuarioId)
                .orElse(new PreferenciasAlertas(usuarioId));

        // Map DTO to Entity
        mapDTOToEntity(dto, preferencias);

        PreferenciasAlertas guardadas = preferenciasRepository.save(preferencias);
        return convertirADTO(guardadas);
    }

    /**
     * Resetea las preferencias de un usuario a los valores por defecto.
     *
     * @param usuarioId ID del usuario
     * @return DTO con las preferencias por defecto
     */
    @Transactional
    public PreferenciasFinancierasDTO resetearPreferencias(Long usuarioId) {
        PreferenciasAlertas preferencias = new PreferenciasAlertas(usuarioId);
        PreferenciasAlertas guardadas = preferenciasRepository.save(preferencias);
        return convertirADTO(guardadas);
    }

    /**
     * Crea una instancia de preferencias con valores por defecto (sin persistir).
     *
     * @param usuarioId ID del usuario
     * @return PreferenciasAlertas con valores por defecto
     */
    private PreferenciasAlertas crearPreferenciasDefault(Long usuarioId) {
        return new PreferenciasAlertas(usuarioId);
    }

    /**
     * Convierte una entidad de preferencias a DTO.
     *
     * @param preferencias Entidad de preferencias
     * @return DTO de preferencias
     */
    private PreferenciasFinancierasDTO convertirADTO(PreferenciasAlertas preferencias) {
        PreferenciasFinancierasDTO dto = new PreferenciasFinancierasDTO();

        // Existentes
        dto.setUmbralAdvertenciaPorcentaje(preferencias.getUmbralAdvertenciaPorcentaje());
        dto.setEgresoGrandePorcentaje(preferencias.getEgresoGrandePorcentaje());
        dto.setAlertaEgresoGrandeActiva(preferencias.getAlertaEgresoGrandeActiva());

        // Tendencias
        dto.setAlertGastoIncrementalEnabled(preferencias.getAlertGastoIncrementalEnabled());
        dto.setAlertGastoIncrementalPorcentaje(preferencias.getAlertGastoIncrementalPorcentaje());
        dto.setAlertGastoIncrementalMeses(preferencias.getAlertGastoIncrementalMeses());
        dto.setAlertReduccionIngresosEnabled(preferencias.getAlertReduccionIngresosEnabled());
        dto.setAlertReduccionIngresosPorcentaje(preferencias.getAlertReduccionIngresosPorcentaje());
        dto.setAlertPatronInusualEnabled(preferencias.getAlertPatronInusualEnabled());

        // Conceptos
        dto.setAlertConcentracionGastosEnabled(preferencias.getAlertConcentracionGastosEnabled());
        dto.setAlertConcentracionGastosPorcentaje(preferencias.getAlertConcentracionGastosPorcentaje());
        dto.setAlertConceptoSinUsoEnabled(preferencias.getAlertConceptoSinUsoEnabled());
        dto.setAlertConceptoSinUsoDias(preferencias.getAlertConceptoSinUsoDias());

        // Tiempo
        dto.setAlertVelocidadGastoEnabled(preferencias.getAlertVelocidadGastoEnabled());
        dto.setAlertInactividadIngresosEnabled(preferencias.getAlertInactividadIngresosEnabled());
        dto.setAlertInactividadDias(preferencias.getAlertInactividadDias());
        dto.setAlertEgresosAgrupadosEnabled(preferencias.getAlertEgresosAgrupadosEnabled());
        dto.setAlertEgresosAgrupadosCantidad(preferencias.getAlertEgresosAgrupadosCantidad());
        dto.setAlertEgresosAgrupadosHoras(preferencias.getAlertEgresosAgrupadosHoras());

        // Ahorro/Balance
        dto.setMetaAhorroMensual(preferencias.getMetaAhorroMensual());
        dto.setAlertMetaAhorroEnabled(preferencias.getAlertMetaAhorroEnabled());
        dto.setAlertBalanceCriticoEnabled(preferencias.getAlertBalanceCriticoEnabled());

        // Sangría
        dto.setAlertMicroGastosEnabled(preferencias.getAlertMicroGastosEnabled());
        dto.setAlertMicroGastosCantidad(preferencias.getAlertMicroGastosCantidad());
        dto.setAlertMicroGastosMontoMax(preferencias.getAlertMicroGastosMontoMax());
        dto.setAlertGastosHormigaEnabled(preferencias.getAlertGastosHormigaEnabled());
        dto.setAlertGastosHormigaMontoMax(preferencias.getAlertGastosHormigaMontoMax());

        // Predictivas
        dto.setAlertProyeccionSobregastoEnabled(preferencias.getAlertProyeccionSobregastoEnabled());
        dto.setAlertComparacionPeriodoEnabled(preferencias.getAlertComparacionPeriodoEnabled());
        dto.setAlertDiaMesCriticoEnabled(preferencias.getAlertDiaMesCriticoEnabled());
        dto.setAlertDiaMesCriticoPorcentaje(preferencias.getAlertDiaMesCriticoPorcentaje());

        // Inconsistencias
        dto.setAlertEgresoSinConceptoEnabled(preferencias.getAlertEgresoSinConceptoEnabled());
        dto.setAlertEgresoSinConceptoCantidad(preferencias.getAlertEgresoSinConceptoCantidad());
        dto.setAlertIngresoInusualEnabled(preferencias.getAlertIngresoInusualEnabled());
        dto.setAlertIngresoInusualMultiplicador(preferencias.getAlertIngresoInusualMultiplicador());

        return dto;
    }

    /**
     * Map DTO to Entity (inverse conversion).
     *
     * @param dto          DTO con los datos
     * @param preferencias Entidad a actualizar
     */
    private void mapDTOToEntity(PreferenciasFinancierasDTO dto, PreferenciasAlertas preferencias) {
        // Existentes
        if (dto.getUmbralAdvertenciaPorcentaje() != null) {
            preferencias.setUmbralAdvertenciaPorcentaje(dto.getUmbralAdvertenciaPorcentaje());
        }
        if (dto.getEgresoGrandePorcentaje() != null) {
            preferencias.setEgresoGrandePorcentaje(dto.getEgresoGrandePorcentaje());
        }
        if (dto.getAlertaEgresoGrandeActiva() != null) {
            preferencias.setAlertaEgresoGrandeActiva(dto.getAlertaEgresoGrandeActiva());
        }

        // Tendencias
        if (dto.getAlertGastoIncrementalEnabled() != null) {
            preferencias.setAlertGastoIncrementalEnabled(dto.getAlertGastoIncrementalEnabled());
        }
        if (dto.getAlertGastoIncrementalPorcentaje() != null) {
            preferencias.setAlertGastoIncrementalPorcentaje(dto.getAlertGastoIncrementalPorcentaje());
        }
        if (dto.getAlertGastoIncrementalMeses() != null) {
            preferencias.setAlertGastoIncrementalMeses(dto.getAlertGastoIncrementalMeses());
        }
        if (dto.getAlertReduccionIngresosEnabled() != null) {
            preferencias.setAlertReduccionIngresosEnabled(dto.getAlertReduccionIngresosEnabled());
        }
        if (dto.getAlertReduccionIngresosPorcentaje() != null) {
            preferencias.setAlertReduccionIngresosPorcentaje(dto.getAlertReduccionIngresosPorcentaje());
        }
        if (dto.getAlertPatronInusualEnabled() != null) {
            preferencias.setAlertPatronInusualEnabled(dto.getAlertPatronInusualEnabled());
        }

        // Conceptos
        if (dto.getAlertConcentracionGastosEnabled() != null) {
            preferencias.setAlertConcentracionGastosEnabled(dto.getAlertConcentracionGastosEnabled());
        }
        if (dto.getAlertConcentracionGastosPorcentaje() != null) {
            preferencias.setAlertConcentracionGastosPorcentaje(dto.getAlertConcentracionGastosPorcentaje());
        }
        if (dto.getAlertConceptoSinUsoEnabled() != null) {
            preferencias.setAlertConceptoSinUsoEnabled(dto.getAlertConceptoSinUsoEnabled());
        }
        if (dto.getAlertConceptoSinUsoDias() != null) {
            preferencias.setAlertConceptoSinUsoDias(dto.getAlertConceptoSinUsoDias());
        }

        // Tiempo
        if (dto.getAlertVelocidadGastoEnabled() != null) {
            preferencias.setAlertVelocidadGastoEnabled(dto.getAlertVelocidadGastoEnabled());
        }
        if (dto.getAlertInactividadIngresosEnabled() != null) {
            preferencias.setAlertInactividadIngresosEnabled(dto.getAlertInactividadIngresosEnabled());
        }
        if (dto.getAlertInactividadDias() != null) {
            preferencias.setAlertInactividadDias(dto.getAlertInactividadDias());
        }
        if (dto.getAlertEgresosAgrupadosEnabled() != null) {
            preferencias.setAlertEgresosAgrupadosEnabled(dto.getAlertEgresosAgrupadosEnabled());
        }
        if (dto.getAlertEgresosAgrupadosCantidad() != null) {
            preferencias.setAlertEgresosAgrupadosCantidad(dto.getAlertEgresosAgrupadosCantidad());
        }
        if (dto.getAlertEgresosAgrupadosHoras() != null) {
            preferencias.setAlertEgresosAgrupadosHoras(dto.getAlertEgresosAgrupadosHoras());
        }

        // Ahorro/Balance
        if (dto.getMetaAhorroMensual() != null) {
            preferencias.setMetaAhorroMensual(dto.getMetaAhorroMensual());
        }
        if (dto.getAlertMetaAhorroEnabled() != null) {
            preferencias.setAlertMetaAhorroEnabled(dto.getAlertMetaAhorroEnabled());
        }
        if (dto.getAlertBalanceCriticoEnabled() != null) {
            preferencias.setAlertBalanceCriticoEnabled(dto.getAlertBalanceCriticoEnabled());
        }

        // Sangría
        if (dto.getAlertMicroGastosEnabled() != null) {
            preferencias.setAlertMicroGastosEnabled(dto.getAlertMicroGastosEnabled());
        }
        if (dto.getAlertMicroGastosCantidad() != null) {
            preferencias.setAlertMicroGastosCantidad(dto.getAlertMicroGastosCantidad());
        }
        if (dto.getAlertMicroGastosMontoMax() != null) {
            preferencias.setAlertMicroGastosMontoMax(dto.getAlertMicroGastosMontoMax());
        }
        if (dto.getAlertGastosHormigaEnabled() != null) {
            preferencias.setAlertGastosHormigaEnabled(dto.getAlertGastosHormigaEnabled());
        }
        if (dto.getAlertGastosHormigaMontoMax() != null) {
            preferencias.setAlertGastosHormigaMontoMax(dto.getAlertGastosHormigaMontoMax());
        }

        // Predictivas
        if (dto.getAlertProyeccionSobregastoEnabled() != null) {
            preferencias.setAlertProyeccionSobregastoEnabled(dto.getAlertProyeccionSobregastoEnabled());
        }
        if (dto.getAlertComparacionPeriodoEnabled() != null) {
            preferencias.setAlertComparacionPeriodoEnabled(dto.getAlertComparacionPeriodoEnabled());
        }
        if (dto.getAlertDiaMesCriticoEnabled() != null) {
            preferencias.setAlertDiaMesCriticoEnabled(dto.getAlertDiaMesCriticoEnabled());
        }
        if (dto.getAlertDiaMesCriticoPorcentaje() != null) {
            preferencias.setAlertDiaMesCriticoPorcentaje(dto.getAlertDiaMesCriticoPorcentaje());
        }

        // Inconsistencias
        if (dto.getAlertEgresoSinConceptoEnabled() != null) {
            preferencias.setAlertEgresoSinConceptoEnabled(dto.getAlertEgresoSinConceptoEnabled());
        }
        if (dto.getAlertEgresoSinConceptoCantidad() != null) {
            preferencias.setAlertEgresoSinConceptoCantidad(dto.getAlertEgresoSinConceptoCantidad());
        }
        if (dto.getAlertIngresoInusualEnabled() != null) {
            preferencias.setAlertIngresoInusualEnabled(dto.getAlertIngresoInusualEnabled());
        }
        if (dto.getAlertIngresoInusualMultiplicador() != null) {
            preferencias.setAlertIngresoInusualMultiplicador(dto.getAlertIngresoInusualMultiplicador());
        }
    }
}
