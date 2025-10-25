package com.bustransport.user.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Service API",
                version = "1.0",
                description = "User management and authentication microservice for Urban Transport Management System",
                contact = @Contact(
                        name = "Urban Transport Team",
                        email = "support@urbantransport.com"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development",
                        url = "http://localhost:8081/api/v1"
                ),
                @Server(
                        description = "Production",
                        url = "https://api.urbantransport.com/user-service"
                )
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfig {
}