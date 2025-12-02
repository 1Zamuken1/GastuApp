package GastuApp.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PerfilViewController {

    @GetMapping("/perfil")
    public String perfil(
            @RequestParam(required = false, defaultValue = "general") String tab, 
            Model model) {
        
        // Set active page for sidebar highlighting
        model.addAttribute("activePage", "perfil");
        
        // Set active tab for content display
        model.addAttribute("activeTab", tab);
        
        return "perfil/index";
    }
    
    // Redirect old routes to unified /perfil
    @GetMapping("/preferencias")
    public String preferencias() {
        return "redirect:/perfil?tab=preferencias";
    }
    
    @GetMapping("/notificaciones")
    public String notificaciones() {
        return "redirect:/perfil?tab=notificaciones";
    }
}
