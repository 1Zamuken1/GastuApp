package GastuApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
@ComponentScan(basePackages = "GastuApp")
public class GastuAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(GastuAppApplication.class, args);
	}

}
