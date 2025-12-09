package GastuApp.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import GastuApp.DTO.DashboardDTO;
import GastuApp.Service.DashboardService;
import GastuApp.User.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            Model model,
            Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }

        Long usuarioId = ((CustomUserDetails) authentication.getPrincipal()).getId();

        // Validate params and ensure valid range
        LocalDate now = LocalDate.now();
        if (mes == null || mes < 1 || mes > 12) {
            mes = now.getMonthValue();
        }
        if (anio == null || anio < 1900 || anio > 2100) {
            anio = now.getYear();
        }

        try {
            DashboardDTO dashboardData = dashboardService.obtenerDatosDashboard(usuarioId, mes, anio);
            model.addAttribute("dashboard", dashboardData);
        } catch (Exception e) {
            // Log the error and provide empty dashboard data
            System.err.println("Error loading dashboard for mes=" + mes + ", anio=" + anio + ": " + e.getMessage());
            e.printStackTrace();
            DashboardDTO emptyDashboard = new DashboardDTO();
            emptyDashboard.setMesNumero(mes);
            emptyDashboard.setAnioActual(anio);
            emptyDashboard.setMesActual(java.time.Month.of(mes).getDisplayName(
                    java.time.format.TextStyle.FULL, new java.util.Locale("es", "ES")));
            emptyDashboard.setTotalIngresos(java.math.BigDecimal.ZERO);
            emptyDashboard.setTotalEgresos(java.math.BigDecimal.ZERO);
            emptyDashboard.setTotalAhorros(java.math.BigDecimal.ZERO);
            emptyDashboard.setBalance(java.math.BigDecimal.ZERO);
            emptyDashboard.setChartLabels(java.util.Collections.emptyList());
            emptyDashboard.setChartIngresos(java.util.Collections.emptyList());
            emptyDashboard.setChartEgresos(java.util.Collections.emptyList());
            emptyDashboard.setChartAhorros(java.util.Collections.emptyList());
            emptyDashboard.setChartBalance(java.util.Collections.emptyList());
            model.addAttribute("dashboard", emptyDashboard);
        }
        model.addAttribute("activePage", "dashboard");

        return "dashboard/index";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
