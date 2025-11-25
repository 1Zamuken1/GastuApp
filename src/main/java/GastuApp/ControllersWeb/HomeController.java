package GastuApp.ControllersWeb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @GetMapping({"/admin/home", "/instructor/home", "/aprendiz/home", "/home"})
    public String home() { return "home"; }
}
