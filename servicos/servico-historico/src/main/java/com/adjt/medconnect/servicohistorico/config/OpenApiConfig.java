package com.adjt.medconnect.servicohistorico.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MedConnect - Serviço de Histórico")
                .description("API de histórico de consultas e dados via GraphQL")
                .version("1.0"));
    }
}