package com.bustransport.route.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI routeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Route Service API")
                        .description("API for managing transit routes, stops, schedules, and path planning")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Urban Transport Team")
                                .email("support@urbantransport.com")));
    }
}

