package com.example.bookvexe_payment_service.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Your Project API", version = "v1", description = "API Documentation for the project"))
@SecurityScheme(name = "Bearer Authentication", // Name used in the SecurityRequirement annotation
    type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer", description = "Provide the JWT Bearer Token.")
public class SwaggerConfig {
    // Configuration is done via annotations. No bean definitions needed here.
}
