package com.bustransport.ticket.config;

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
                title = "Ticket Service API",
                version = "1.0",
                description = "Ticket, Order, Payment and Refund management microservice for Urban Transport Management System",
                contact = @Contact(
                        name = "Urban Transport Team",
                        email = "support@urbantransport.com"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development",
                        url = "http://localhost:8083"
                ),
                @Server(
                        description = "Production",
                        url = "https://api.urbantransport.com/ticket-service"
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
