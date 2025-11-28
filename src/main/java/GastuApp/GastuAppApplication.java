package GastuApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "GastuApp")
public class GastuAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(GastuAppApplication.class, args);
	}

}
