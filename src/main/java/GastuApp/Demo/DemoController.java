package GastuApp.Demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor


public class DemoController {

    @RequestMapping(value = "demo")
    public String welcome() {
        return "welcome form secure endpoint";
    }

}
