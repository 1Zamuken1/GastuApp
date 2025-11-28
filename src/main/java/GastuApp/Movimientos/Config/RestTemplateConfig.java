package GastuApp.Movimientos.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuracion para habilitar RestTemplate en el contexto de Spring.
 * RestTemplate se utiliza para realizar llamadas HTTP a otros microservicios.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Define un bean de RestTemplate para inyeccion de dependencias.
     * Permite realizar peticiones HTTP REST a servicios externos.
     *
     * @return Instancia de RestTemplate configurada
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}