package com.freddieapp.loanorigination.config;

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
                        .title("Loan Origination Service API")
                        .version("1.0.0")
                        .description("Mortgage Application Lifecycle & Origination REST API")
                        .contact(new Contact().name("Freddie Mac Platform Team")));
    }
}
