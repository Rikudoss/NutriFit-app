package kz.nutrifit.backend.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI nutrifitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NutriFit API")
                        .description("API documentation for NutriFit AI backend")
                        .version("1.0.0"));
    }
}
