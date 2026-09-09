package com.freddieapp.documentservice.config;

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
                        .title("Document Service API")
                        .version("1.0.0")
                        .description("Reactive Document Upload & Storage REST API")
                        .contact(new Contact().name("Freddie Mac Platform Team")));
    }
}
