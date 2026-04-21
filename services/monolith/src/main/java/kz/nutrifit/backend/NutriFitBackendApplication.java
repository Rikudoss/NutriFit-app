package kz.nutrifit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NutriFitBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(NutriFitBackendApplication.class, args);
    }
}
