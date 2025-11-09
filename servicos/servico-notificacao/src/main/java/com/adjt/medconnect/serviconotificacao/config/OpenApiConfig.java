package com.adjt.medconnect.serviconotificacao.config;

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
                .title("MedConnect - Serviço de Notificação")
                .description("API de notificações e lembretes aos pacientes")
                .version("1.0"));
    }
}