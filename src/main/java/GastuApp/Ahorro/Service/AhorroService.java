package GastuApp.Ahorro.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import GastuApp.Ahorro.DTO.AhorroDTO;
import GastuApp.Ahorro.DTO.AporteAhorroDTO;
import GastuApp.Ahorro.DTO.CrearAhorroDTO;
import GastuApp.Ahorro.DTO.EditarAhorroDTO;
import GastuApp.Ahorro.Entity.AhorroMeta;
import GastuApp.Ahorro.Entity.AhorroMeta.Estado;
import GastuApp.Ahorro.Entity.AhorroMeta.Frecuencia;
import GastuApp.Ahorro.Entity.AporteAhorro;
import GastuApp.Ahorro.Repository.AhorroMetaRepository;
import GastuApp.Ahorro.Repository.AporteAhorroRepository;
import GastuApp.Conceptos.DTO.ConceptoDTO;
import GastuApp.Conceptos.Service.ConceptoService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AhorroService {
    
    private final AhorroMetaRepository ahorroMetaRepository;
    private final AporteAhorroRepository aporteAhorroRepository;
    private final ConceptoService conceptoService;

    public AhorroService(AhorroMetaRepository ahorroMetaRepository,
                        AporteAhorroRepository aporteAhorroRepository,
                        ConceptoService conceptoService) {
        this.ahorroMetaRepository = ahorroMetaRepository;
        this.aporteAhorroRepository = aporteAhorroRepository;
        this.conceptoService = conceptoService;
    }

    // ---------------- MAPEADORES ----------------

    private AhorroMeta toEntity(CrearAhorroDTO dto, Long usuarioId) {
        AhorroMeta a = new AhorroMeta();
        a.setUsuarioId(usuarioId);
        a.setConceptoId(dto.getConceptoId());
        a.setDescripcion(dto.getDescripcion());
        if (dto.getMonto() != null) a.setMonto(dto.getMonto());
        a.setFrecuencia(dto.getFrecuencia());
        if (dto.getMeta() != null) a.setMeta(dto.getMeta());
        if (dto.getCantCuotas() != null) a.setCantCuotas(dto.getCantCuotas());
        a.setAcumulado(BigDecimal.ZERO);
        a.setEstado(Estado.SININICIAR);
        return a;
    }

        private AhorroDTO toDTO(AhorroMeta a) {
        AhorroDTO dto = new AhorroDTO();
        dto.setId(a.getId());
        dto.setConceptoId(a.getConceptoId());
        dto.setDescripcion(a.getDescripcion());
        dto.setMonto(a.getMonto());
        dto.setAcumulado(a.getAcumulado());
        dto.setFrecuencia(a.getFrecuencia());
        dto.setCreacion(a.getCreacion());
        dto.setMeta(a.getMeta());
        dto.setEstado(a.getEstado());
        dto.setCantCuotas(a.getCantCuotas());

        ConceptoDTO c = conceptoService.obtenerPorId(a.getConceptoId());
        
        if (c != null) {
            dto.setNombreConcepto(c.getNombre());
        } else {
            throw new DataIntegrityViolationException("Concepto asociado no encontrado para Ahorro ID: " + a.getId());
        }
        return dto; 
    }

    public AporteAhorroDTO toAporteDTO(AporteAhorro ap) {
        AporteAhorroDTO dtoAp = new AporteAhorroDTO();
        dtoAp.setMetaId(ap.getMetaId());
        dtoAp.setAporteAhorroId(ap.getAporteAhorroId());
        dtoAp.setAporteAsignado(ap.getAporteAsignado());
        dtoAp.setAporte(ap.getAporte());
        dtoAp.setFechaLimite(ap.getFechaLimite());
        dtoAp.setEstado(ap.getEstado());
        return dtoAp;
    }

    //-----------------CRUD AHORRRO META----------------------

    //VER todo por usuario
    @Transactional(readOnly = true)
    public List<AhorroDTO> listarTodosPorUsuario(Long usuarioId) {
        return ahorroMetaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ver un ahorro por id
    @Transactional(readOnly = true)
    public AhorroDTO obtenerPorIdYUsuario(Long id, Long usuarioId) {
        AhorroMeta existente = ahorroMetaRepository.findByAhorroIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Ahorro no encontrado o sin permisos"));
        return toDTO(existente);
    }

//ver un concepto por nombre
    @Transactional(readOnly = true)
    public List<AhorroDTO> buscarPorConceptoParcial(Long usuarioId, String texto) {
        return ahorroMetaRepository.buscarPorConceptoParcial(usuarioId, texto)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

// filtrar por estado
    @Transactional(readOnly = true)
    public List<AhorroDTO> filtrarPorEstado(Long usuarioId, Estado estado) {
        return ahorroMetaRepository.findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(usuarioId, estado)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

// METODO PARA VALIDAR EL CONCEPTO
        private void validarConcepto(Long conceptoId) {
        if (conceptoId == null) {
            throw new IllegalArgumentException("El concepto es requerido");
        }
        ConceptoDTO concepto = conceptoService.obtenerPorId(conceptoId);
        if (!"AHORRO".equals(concepto.getTipo())) {
        throw new IllegalArgumentException("El concepto debe ser de tipo AHORRO");
    }
    }

// METODO QUE VALIDA QUE CONCEPTO, FRECUENCIA Y MONTO META SEAN OBLIGATORIOS
    private void validarCamposCrearAhorro(CrearAhorroDTO dto) {
    if (dto.getConceptoId() == null) {
        throw new IllegalArgumentException("El concepto es obligatorio");
    }
    if (dto.getFrecuencia() == null) {
        throw new IllegalArgumentException("La frecuencia es obligatoria");
    }
    
    if (dto.getMonto() == null || dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("El monto meta es obligatorio y debe ser mayor que cero.");
    }

    int ingreso = 0;
    if (dto.getMeta() != null) ingreso++;
    if (dto.getCantCuotas() != null) ingreso++;
    
    if (ingreso < 1) { 
        throw new IllegalArgumentException("Debes proveer la fecha meta o la cantidad de cuotas (o ambas)");
    }
}

//METODO QUE CALCULA LA FRECUENCIA
    private int calcularPeriodo(Frecuencia frecuencia) {
        if (frecuencia == null) return 30;
        switch (frecuencia) {
            case DIARIA:
                return 1;
            case SEMANAL:
                return 7;
            case QUINCENAL:
                return 15;
            case MENSUAL:
                return 30;
            case TRIMESTRAL:
                return 90;
            case SEMESTRAL:
                return 182;
            case ANUAL:
                return 365;
            default:
                return 30;
        }
    }

// METODO QUE SUMA LA FRECUENCIA
    private LocalDate sumarFrecuencia(LocalDate base, Frecuencia frecuencia, int count) {
        if (base == null) base = LocalDate.now();
        if (count <= 0) return base;
        switch (frecuencia) {
            case DIARIA:
                return base.plusDays(count);
            case SEMANAL:
                return base.plusWeeks(count);
            case QUINCENAL:
                return base.plusDays((long) count * 15);
            case MENSUAL:
                return base.plusMonths(count);
            case TRIMESTRAL:
                return base.plusMonths((long) count * 3);
            case SEMESTRAL:
                return base.plusMonths((long) count * 6);
            case ANUAL:
                return base.plusYears(count);
            default:
                return base.plusDays((long) count * 30);
        }
    }
// METODO QUE CALCULA EL CAMPO FALTANTE
    private void calcularCampoFaltante(AhorroMeta entidad, CrearAhorroDTO dto) {
        // Asegurar fechaCreacion no nula
        if (entidad.getCreacion() == null) {
            entidad.setCreacion(LocalDate.now());
        }

        boolean tieneMonto = entidad.getMonto() != null;
        boolean tieneFecha = entidad.getMeta() != null;
        boolean tieneCantidad = entidad.getCantCuotas() != null;

        //  falta cantidadCuotas -> calcular por periodos entre creacion y fechaMeta
        if (!tieneCantidad && tieneFecha && tieneMonto) {
            int periodoDias = calcularPeriodo(entidad.getFrecuencia());
            long dias = ChronoUnit.DAYS.between(entidad.getCreacion(), entidad.getMeta());
            if (dias < 0) dias = 0;
            int cantidad = (int) Math.max(1, Math.ceil((double) (dias + 1) / periodoDias));
            entidad.setCantCuotas(cantidad);
            return;
        }

        //  falta fechaMeta -> calcular sumando (cantidadCuotas -1) periodos a la fechaCreacion
        if (!tieneFecha && tieneCantidad) {
            LocalDate inicio = entidad.getCreacion();
            // Si la cantidad es 1 -> fechaMeta = inicio
            if (entidad.getCantCuotas() <= 1) {
                entidad.setMeta(inicio);
            } else {
                // sumar (cantidad-1) periodos
                LocalDate fechaMeta = sumarFrecuencia(inicio, entidad.getFrecuencia(), entidad.getCantCuotas() - 1);
                entidad.setMeta(fechaMeta);
            }
            return;
        }
        //  nada que calcular
        if (tieneMonto && tieneFecha && tieneCantidad) {
            return;
        }

        // fallback (no debería llegar por validación previa)
        throw new IllegalArgumentException("No se pudo calcular campos faltantes: combinación inválida");
    }

//METODO QUE GENERA LAS CUOTAS
    private List<AporteAhorro> generarCuotas(AhorroMeta meta) {
        List<AporteAhorro> resultado = new ArrayList<>();
        Integer obj = meta.getCantCuotas();
        int n = (obj == null) ? 0 : obj;
        if (n <= 0) return resultado;

        BigDecimal monto = meta.getMonto() == null ? BigDecimal.ZERO : meta.getMonto();
        BigDecimal cuotaBase = BigDecimal.ZERO;
        if (monto.compareTo(BigDecimal.ZERO) > 0) {
            cuotaBase = monto.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        }

        LocalDate inicio = meta.getCreacion() == null ? LocalDate.now() : meta.getCreacion();

        for (int i = 0; i < n; i++) {
            AporteAhorro ap = new AporteAhorro();
            ap.setMetaId(meta.getId());
            ap.setAporteAsignado(cuotaBase);
            ap.setAporte(BigDecimal.ZERO);
            ap.setEstado(AporteAhorro.EstadoAp.PENDIENTE);
            LocalDate fechaLimite = sumarFrecuencia(inicio, meta.getFrecuencia(), i);
            ap.setFechaLimite(fechaLimite);
            resultado.add(ap);
        }

        // Ajuste por redondeo: sumar diferencias en la última cuota
        if (monto.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sumaAsignados = resultado.stream()
                    .map(r -> r.getAporteAsignado() == null ? BigDecimal.ZERO : r.getAporteAsignado())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal diff = monto.subtract(sumaAsignados).setScale(2, RoundingMode.HALF_UP);
            if (diff.compareTo(BigDecimal.ZERO) != 0 && !resultado.isEmpty()) {
                AporteAhorro ultima = resultado.get(resultado.size() - 1);
                BigDecimal nuevo = (ultima.getAporteAsignado() == null ? BigDecimal.ZERO : ultima.getAporteAsignado()).add(diff);
                ultima.setAporteAsignado(nuevo);
            }
        } else {
            // monto = 0 -> dejar aporteAsignado = 0 explicitamente
            resultado.forEach(r -> r.setAporteAsignado(BigDecimal.ZERO));
        }

        return resultado;
    }

// Crear ahorro 
    @Transactional
    public AhorroDTO crear(CrearAhorroDTO dto, Long usuarioId) {
        validarConcepto(dto.getConceptoId());
        validarCamposCrearAhorro(dto);

        AhorroMeta entidad = toEntity(dto, usuarioId);

        // asegurar fechaCreacion (si no está por @PrePersist aún)
        if (entidad.getCreacion() == null) {
            entidad.setCreacion(LocalDate.now());
        }

        // calcular campo faltante (implementación completa abajo)
        calcularCampoFaltante(entidad, dto);

        // totalAcumulado y estado por defecto
        if (entidad.getAcumulado() == null) entidad.setAcumulado(BigDecimal.ZERO);
        if (entidad.getEstado() == null) entidad.setEstado(Estado.SININICIAR);

        // persistir meta para obtener ID
        AhorroMeta guardado = ahorroMetaRepository.save(entidad);

        // generar cuotas con fechas y aportes asignados precisos
        List<AporteAhorro> cuotas = generarCuotas(guardado);
        if (!cuotas.isEmpty()) {
            aporteAhorroRepository.saveAll(cuotas);
        }

        return toDTO(guardado);
    }

//METODO QUE CALCULA LOS APORTES RESTANTES
    private void recalcularAportesRestantes(AhorroMeta meta) {
        List<AporteAhorro> todas = aporteAhorroRepository.findByMetaIdOrderByFechaLimiteAsc(meta.getId());

        BigDecimal aportado = todas.stream()
                .filter(a -> a.getEstado() == AporteAhorro.EstadoAp.APORTADO)
                .map(a -> a.getAporte() == null ? BigDecimal.ZERO : a.getAporte())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AporteAhorro> pendientes = todas.stream()
                .filter(a -> a.getEstado() == AporteAhorro.EstadoAp.PENDIENTE)
                .collect(Collectors.toList());

        int restantes = pendientes.size();
        BigDecimal montoMeta = meta.getMonto() == null ? BigDecimal.ZERO : meta.getMonto();
        BigDecimal restante = montoMeta.subtract(aportado);
        if (restante.compareTo(BigDecimal.ZERO) <= 0) {
            // si ya cumplido, poner 0 a pendientes
            pendientes.forEach(p -> {
                p.setAporteAsignado(BigDecimal.ZERO);
                aporteAhorroRepository.save(p);
            });
            return;
        }
        // reparto igual entre pendientes con ajuste
        BigDecimal asignadoBase = restante.divide(BigDecimal.valueOf(restantes), 2, RoundingMode.HALF_UP);
        for (int i = 0; i < pendientes.size(); i++) {
            AporteAhorro p = pendientes.get(i);
            p.setAporteAsignado(asignadoBase);
            aporteAhorroRepository.save(p);
        }

        // corregir diferencia en la última pendiente
        BigDecimal sumaAsignados = pendientes.stream()
                .map(a -> a.getAporteAsignado() == null ? BigDecimal.ZERO : a.getAporteAsignado())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal diff = restante.subtract(sumaAsignados).setScale(2, RoundingMode.HALF_UP);
        if (diff.compareTo(BigDecimal.ZERO) != 0 && !pendientes.isEmpty()) {
            AporteAhorro last = pendientes.get(pendientes.size() - 1);
            last.setAporteAsignado((last.getAporteAsignado() == null ? BigDecimal.ZERO : last.getAporteAsignado()).add(diff));
            aporteAhorroRepository.save(last);
        }
    }

//METODO QUE ME RECALCULA LAS CUOTAS AL EDITAR UN AHORRO
    private void recalcularAportes(AhorroMeta meta) {
        List<AporteAhorro> todas = aporteAhorroRepository.findByMetaIdOrderByFechaLimiteAsc(meta.getId());

        // separar aportadas vs pendientes
        List<AporteAhorro> aportadas = todas.stream()
                .filter(a -> a.getEstado() == AporteAhorro.EstadoAp.APORTADO)
                .collect(Collectors.toList());

        List<AporteAhorro> pendientes = todas.stream()
                .filter(a -> a.getEstado() == AporteAhorro.EstadoAp.PENDIENTE)
                .collect(Collectors.toList());

        // generar nuevas fechas para las pendientes según la nueva configuración
        // estrategia: empezar desde fechaCreacion y saltar periodos, pero respetar el número total de cuotas
        Integer cantidad = meta.getCantCuotas() == null ? (aportadas.size() + pendientes.size()) : meta.getCantCuotas();
        LocalDate inicio = meta.getCreacion() == null ? LocalDate.now() : meta.getCreacion();

        // Generamos la lista completa de fechas para 'cantidad' cuotas y luego reasignamos las fechas
        List<LocalDate> fechas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            fechas.add(sumarFrecuencia(inicio, meta.getFrecuencia(), i));
        }

        // Mantener las aportadas en sus posiciones iniciales por fecha si coinciden
        // reasignar a las pendientes las fechas que no estén ocupadas por aportadas
        Set<LocalDate> fechasAportadas = aportadas.stream().map(AporteAhorro::getFechaLimite).collect(Collectors.toSet());
        Iterator<LocalDate> it = fechas.stream().filter(f -> !fechasAportadas.contains(f)).iterator();
        for (AporteAhorro p : pendientes) {
            if (it.hasNext()) {
                p.setFechaLimite(it.next());
                aporteAhorroRepository.save(p);
            }
        }
        // finalmente, recalcular aporteAsignado en pendientes
        recalcularAportesRestantes(meta);
    }

// editar un ahorro
    @Transactional
    public AhorroDTO actualizar(Long id, EditarAhorroDTO dto, Long usuarioId) {
        AhorroMeta existente = ahorroMetaRepository.findByAhorroIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ahorro no encontrado o sin permisos"));

        if (dto.getFrecuencia() == null) {
            throw new IllegalArgumentException("La frecuencia es obligatoria");
        }

        if (dto.getDescripcion() != null) existente.setDescripcion(dto.getDescripcion());
        if (dto.getMonto() != null) existente.setMonto(dto.getMonto());
        existente.setFrecuencia(dto.getFrecuencia());
        if (dto.getMeta() != null) existente.setMeta(dto.getMeta());
        if (dto.getCantCuotas() != null) existente.setCantCuotas(dto.getCantCuotas());

        // Recalcular cuotas pendientes (no tocar las aportadas)
        recalcularAportes(existente);

        AhorroMeta actualizado = ahorroMetaRepository.save(existente);
        return toDTO(actualizado);
    }

// eliminar un ahorro por id
    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        AhorroMeta existente = ahorroMetaRepository.findByAhorroIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ahorro no encontrado o sin permisos"));
        List<AporteAhorro> aportes = aporteAhorroRepository.findByMetaIdOrderByFechaLimiteAsc(existente.getId());
        if (!aportes.isEmpty()) {
            aporteAhorroRepository.deleteAll(aportes);
        }
        ahorroMetaRepository.delete(existente);
    }

//-------------CREAR Y LISTAR APORTES---------------------

// Ver todos los aporte de un ahorro especifico
@Transactional(readOnly = true)
    public List<AporteAhorroDTO> listarAportesPorMeta(Long metaId, Long usuarioId) {
        ahorroMetaRepository.findByAhorroIdAndUsuarioId(metaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Ahorro no encontrado o sin permisos"));
        return aporteAhorroRepository.findByMetaIdOrderByFechaLimiteAsc(metaId)
                .stream()
                .map(this::toAporteDTO)
                .collect(Collectors.toList());
    }

//METODO QUE PASA EL ESTADO DE LAS CUOTAS A PERDIDA
   private void pasarCuotasAPerdias(AhorroMeta meta) {
        LocalDate hoy = LocalDate.now();
        List<AporteAhorro> todas = aporteAhorroRepository.findByMetaIdOrderByFechaLimiteAsc(meta.getId());
        boolean changed = false;
        for (AporteAhorro a : todas) {
            if (a.getEstado() == AporteAhorro.EstadoAp.PENDIENTE && a.getFechaLimite().isBefore(hoy)) {
                a.setEstado(AporteAhorro.EstadoAp.PERDIDO);
                aporteAhorroRepository.save(a);
                changed = true;
            }
        }
    }

//METODO QUE PASA UNA CUOTA A DISPONIBLE
    private boolean cuotaDisponiblePago(AporteAhorro cuota) {
        if (cuota == null) return false;
        LocalDate hoy = LocalDate.now();
        // disponible si fechaLimite <= hoy + 7 días
        return !cuota.getFechaLimite().isAfter(hoy.plusDays(7));
    }

//METODO QUE MUESTRA LOS 3 APORTES
    private <T> List<T> verTresAportes(List<T> list, int n) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        int size = list.size();
        int start = Math.max(0, size - n);
        return list.subList(start, size);
    }

//METODO QUE Al DETECTAR 3 CUOTAS PERDIDAS, PASA EL AHORRO A ESTADO ABANDONADO
    private void abandonoAhorro(AhorroMeta meta) {
        List<AporteAhorro> todas = aporteAhorroRepository.findByMetaIdOrderByFechaLimiteAsc(meta.getId());
        if (todas.size() < 3) return;

        // ordenar por fecha asc (ya lo está) y tomar últimas 3
        List<AporteAhorro> ultimas3 = verTresAportes(todas, 3);
        boolean tresPerdidas = ultimas3.stream()
                .allMatch(a -> a.getEstado() == AporteAhorro.EstadoAp.PERDIDO);
        if (tresPerdidas) {
            meta.setEstado(Estado.ABANDONADO);
        }
    }


//muestra la proxima cuota disponible
    public Optional<AporteAhorro> obtenerCuotaDisponible(Long metaId, Long usuarioId) {
        ahorroMetaRepository.findByAhorroIdAndUsuarioId(metaId, usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Ahorro no encontrado o sin permisos"));

        LocalDate hoy = LocalDate.now();
        List<AporteAhorro> todas = aporteAhorroRepository.findByMetaIdOrderByFechaLimiteAsc(metaId);
        
        return todas.stream()
        .filter(a -> a.getEstado() == AporteAhorro.EstadoAp.PENDIENTE)
        .filter(a -> !a.getFechaLimite().isAfter(hoy.plusDays(7)))
        .findFirst();

    }
   
// registrar un aporte
    @Transactional
    public AporteAhorroDTO registrarAporte(Long metaId, Long aporteId, AporteAhorroDTO dto, Long usuarioId) {
        AhorroMeta meta = ahorroMetaRepository.findByAhorroIdAndUsuarioId(metaId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ahorro no encontrado o sin permisos"));

        //validad que el monto ingresado sea mayor a 0
        BigDecimal aporteIngresado = dto.getAporte() == null ? BigDecimal.ZERO : dto.getAporte();
        if (aporteIngresado.compareTo(BigDecimal.ZERO) <= 0) {
        throw new  IllegalArgumentException("El monto del aporte debe ser mayor que cero para registrar un pago.");
    }        
        // marcar cuotas vencidas como PERDIDO si su fechaLimite < hoy y siguen PENDIENTE
        pasarCuotasAPerdias(meta);

        AporteAhorro cuota;
        if (aporteId != null) {
            cuota = aporteAhorroRepository.findByAporteAhorroIdAndMetaId(aporteId, metaId)
                    .orElseThrow(() -> new RuntimeException("Cuota no encontrada para esta meta"));
        } else {
            cuota = obtenerCuotaDisponible(metaId, usuarioId)
                    .orElseThrow(() -> new RuntimeException("No hay cuota disponible para aportar hoy"));
        }
        // validar que cuota esté PENDIENTE
        if (cuota.getEstado() != AporteAhorro.EstadoAp.PENDIENTE) {
            throw new IllegalArgumentException("La cuota seleccionada no está disponible para aportar (estado=" + cuota.getEstado() + ")");
        }

        // validar disponibilidad 7 días antes
        if (!cuotaDisponiblePago(cuota)) {
            throw new IllegalArgumentException("La cuota no está disponible para pago todavía (solo 7 días antes)");
        }

        // Registrar aporte: actualizar aporte y estado
        cuota.setAporte(aporteIngresado);
        cuota.setEstado(AporteAhorro.EstadoAp.APORTADO);
        aporteAhorroRepository.save(cuota);

        // actualizar acumulado de la meta
        BigDecimal acumuladoActual = meta.getAcumulado() == null ? BigDecimal.ZERO : meta.getAcumulado();
        acumuladoActual = acumuladoActual.add(aporteIngresado);
        meta.setAcumulado(acumuladoActual);

        //  Reanudar o Iniciar: Si se hizo un pago, el estado debe ser ACTIVO, a menos que se complete.
        if (meta.getEstado() == Estado.SININICIAR || meta.getEstado() == Estado.ABANDONADO) {
        meta.setEstado(Estado.ACTIVO);
        }

        //Si total alcanzado o superado -> COMPLETADO
        BigDecimal montoMeta = meta.getMonto() == null ? BigDecimal.ZERO : meta.getMonto();
        if (montoMeta.compareTo(BigDecimal.ZERO) > 0 && acumuladoActual.compareTo(montoMeta) >= 0) {
        meta.setEstado(Estado.COMPLETADO);
        }

        // Si aporte ingresado difiere del asignado -> recalcular cuotas pendientes
        BigDecimal asignado = cuota.getAporteAsignado() == null ? BigDecimal.ZERO : cuota.getAporteAsignado();
        if (aporteIngresado.compareTo(asignado) != 0) {
            recalcularAportesRestantes(meta);
        }

        // detectar abandono DEBE ir después de la lógica de reanudación para capturar el nuevo estado
        abandonoAhorro(meta);
        ahorroMetaRepository.save(meta);

        return toAporteDTO(cuota);
    }

}
