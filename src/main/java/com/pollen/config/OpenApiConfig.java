package com.pollen.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pollenOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pollen API")
                        .description("API REST da plataforma Pollen — acompanhamento de projetos e atividades")
                        .version("0.0.1"));
    }
}
