package edu.jecrc.portal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("JECRC University — Integrated Management Portal API")
                        .description("RESTful Backend Architecture for JECRC University Training and Placement, Academics, Timetable and Administration")
                        .version("1.0.0-PROD")
                        .contact(new Contact()
                                .name("JECRC University TPO & IT Services")
                                .url("https://jecrcuniversity.edu.in")
                                .email("tpo@jecrcu.edu.in"))
                        .license(new License().name("Proprietary - JECRC University")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT Bearer token obtained from /api/v1/auth/login")));
    }
}
