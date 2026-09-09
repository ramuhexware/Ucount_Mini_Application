package com.freddieapp.underwriting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Swagger2Config {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Underwriting Service API")
                        .version("1.0.0")
                        .description("Underwriting Assessment & Risk Scoring REST API")
                        .contact(new Contact().name("Freddie Mac Platform Team")));
    }
}
