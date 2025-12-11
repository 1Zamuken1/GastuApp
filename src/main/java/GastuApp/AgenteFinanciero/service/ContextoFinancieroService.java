package GastuApp.AgenteFinanciero.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ContextoFinancieroService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public String obtenerContextoFinanciero(Long usuarioId) {
        log.info("========================================");
        log.info("INICIANDO OBTENCIÓN DE CONTEXTO FINANCIERO");
        log.info("Usuario ID: {}", usuarioId);
        log.info("========================================");
        
        try {
            verificarUsuarioExiste(usuarioId);
            
            StringBuilder contexto = new StringBuilder();
            contexto.append("DATOS FINANCIEROS DEL USUARIO:\n\n");
            
            BigDecimal balanceTotal = obtenerBalanceTotal(usuarioId);
            contexto.append("Balance Total de Ahorros: $").append(balanceTotal).append("\n");
            
            Map<String, Object> gastosDelMes = obtenerGastosDelMes(usuarioId);
            contexto.append("Gastos del mes actual: $").append(gastosDelMes.get("total")).append("\n");
            contexto.append("Número de transacciones de gastos: ").append(gastosDelMes.get("cantidad")).append("\n");
            
            Map<String, Object> ingresosDelMes = obtenerIngresosDelMes(usuarioId);
            contexto.append("Ingresos del mes actual: $").append(ingresosDelMes.get("total")).append("\n");
            
            String categoriasTop = obtenerTopCategorias(usuarioId);
            if (!categoriasTop.isEmpty()) {
                contexto.append("\nCategorías con más gastos:\n").append(categoriasTop);
            }
            
            Map<String, Object> ahorros = obtenerResumenAhorros(usuarioId);
            contexto.append("\nMetas de Ahorro:\n");
            contexto.append("Total en metas de ahorro: $").append(ahorros.get("total")).append("\n");
            contexto.append("Número de metas activas: ").append(ahorros.get("cantidad")).append("\n");
            
            log.info("CONTEXTO GENERADO EXITOSAMENTE");
            log.info("CONTEXTO COMPLETO:\n{}", contexto);
            
            return contexto.toString();
            
        } catch (Exception e) {
            log.error("ERROR CRÍTICO AL OBTENER CONTEXTO FINANCIERO", e);
            return "DATOS FINANCIEROS DEL USUARIO:\n\nNo se pudieron cargar los datos financieros completos.\nError: " + e.getMessage();
        }
    }
    
    private void verificarUsuarioExiste(Long usuarioId) {
        try {
            String sql = "SELECT COUNT(*) FROM usuario WHERE usuario_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId);
            log.info("Usuario {} existe: {}", usuarioId, count > 0);
            
            if (count == 0) {
                log.warn("USUARIO {} NO ENCONTRADO EN BD", usuarioId);
            }
        } catch (Exception e) {
            log.error("Error verificando usuario", e);
        }
    }
    
    private BigDecimal obtenerBalanceTotal(Long usuarioId) {
        try {
            String sql = "SELECT COALESCE(SUM(total_acumulado), 0) FROM ahorro_meta WHERE usuario_id = ?";
            log.info("Query balance: {} con usuarioId={}", sql, usuarioId);
            
            BigDecimal resultado = jdbcTemplate.queryForObject(sql, BigDecimal.class, usuarioId);
            log.info("✓ Balance obtenido: {}", resultado);
            
            return resultado != null ? resultado : BigDecimal.ZERO;
            
        } catch (Exception e) {
            log.error("✗ Error obteniendo balance", e);
            return BigDecimal.ZERO;
        }
    }
    
    private Map<String, Object> obtenerGastosDelMes(Long usuarioId) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            LocalDateTime inicioMes = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
            
            log.info("Fecha inicio mes: {}", inicioMes);
            
            String sqlTotal = "SELECT COALESCE(SUM(monto), 0) FROM movimiento " +
                    "WHERE usuario_id = ? AND concepto_id IN (SELECT concepto_id FROM concepto WHERE tipo = 'EGRESO') " +
                    "AND fecha_registro >= ?";
            
            // ✅ CORREGIDO: Parámetros directos
            BigDecimal total = jdbcTemplate.queryForObject(
                sqlTotal, 
                BigDecimal.class,
                usuarioId,
                inicioMes
            );
            
            String sqlCantidad = "SELECT COUNT(*) FROM movimiento " +
                    "WHERE usuario_id = ? AND concepto_id IN (SELECT concepto_id FROM concepto WHERE tipo = 'EGRESO') " +
                    "AND fecha_registro >= ?";
            
            Integer cantidad = jdbcTemplate.queryForObject(
                sqlCantidad,
                Integer.class,
                usuarioId,
                inicioMes
            );
            
            resultado.put("total", total != null ? total : BigDecimal.ZERO);
            resultado.put("cantidad", cantidad != null ? cantidad : 0);
            
            log.info("✓ Gastos - Total: {}, Cantidad: {}", resultado.get("total"), resultado.get("cantidad"));
            
        } catch (Exception e) {
            log.error("✗ Error obteniendo gastos", e);
            resultado.put("total", BigDecimal.ZERO);
            resultado.put("cantidad", 0);
        }
        
        return resultado;
    }
    
    private Map<String, Object> obtenerIngresosDelMes(Long usuarioId) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            LocalDateTime inicioMes = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
            
            String sqlTotal = "SELECT COALESCE(SUM(monto), 0) FROM movimiento " +
                    "WHERE usuario_id = ? AND concepto_id IN (SELECT concepto_id FROM concepto WHERE tipo = 'INGRESO') " +
                    "AND fecha_registro >= ?";
            
            BigDecimal total = jdbcTemplate.queryForObject(
                sqlTotal,
                BigDecimal.class,
                usuarioId,
                inicioMes
            );
            
            resultado.put("total", total != null ? total : BigDecimal.ZERO);
            log.info("✓ Ingresos: {}", resultado.get("total"));
            
        } catch (Exception e) {
            log.error("✗ Error obteniendo ingresos", e);
            resultado.put("total", BigDecimal.ZERO);
        }
        
        return resultado;
    }
    
    private String obtenerTopCategorias(Long usuarioId) {
        try {
            LocalDateTime inicioMes = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
            
            String sql = "SELECT c.nombre AS nombre, SUM(m.monto) AS total " +
                    "FROM movimiento m " +
                    "JOIN concepto c ON m.concepto_id = c.concepto_id " +
                    "WHERE m.usuario_id = ? AND c.tipo = 'EGRESO' AND m.fecha_registro >= ? " +
                    "GROUP BY c.nombre " +
                    "ORDER BY total DESC " +
                    "LIMIT 5";
            
            StringBuilder sb = new StringBuilder();
            
            // ✅ CORREGIDO: query con lambda
            jdbcTemplate.query(sql,
                (rs) -> {
                    sb.append("- ").append(rs.getString("nombre"))
                      .append(": $").append(rs.getBigDecimal("total"))
                      .append("\n");
                },
                usuarioId,
                inicioMes
            );
            
            log.info("✓ Top categorías obtenidas");
            return sb.toString();
            
        } catch (Exception e) {
            log.error("✗ Error obteniendo categorías", e);
            return "";
        }
    }
    
    private Map<String, Object> obtenerResumenAhorros(Long usuarioId) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            String sqlTotal = "SELECT COALESCE(SUM(total_acumulado), 0) FROM ahorro_meta WHERE usuario_id = ?";
            String sqlCantidad = "SELECT COUNT(*) FROM ahorro_meta WHERE usuario_id = ?";
            
            BigDecimal total = jdbcTemplate.queryForObject(sqlTotal, BigDecimal.class, usuarioId);
            Integer cantidad = jdbcTemplate.queryForObject(sqlCantidad, Integer.class, usuarioId);
            
            resultado.put("total", total != null ? total : BigDecimal.ZERO);
            resultado.put("cantidad", cantidad != null ? cantidad : 0);
            
            log.info("✓ Ahorros - Total: {}, Cantidad: {}", resultado.get("total"), resultado.get("cantidad"));
            
        } catch (Exception e) {
            log.error("✗ Error obteniendo ahorros", e);
            resultado.put("total", BigDecimal.ZERO);
            resultado.put("cantidad", 0);
        }
        
        return resultado;
    }
}