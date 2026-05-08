package com.carbo.checklist.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${config.oauth2.accessTokenUri}")
    private String accessTokenUri;

    @Value("${carbo.fracproop.service.baseurl}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url(baseUrl+"checklist/") // external route
                ))
                .components(new Components()
                        // OAuth2 Password Flow (matches old swagger)
                        .addSecuritySchemes("oauth2", createOAuth2SecurityScheme())
                        // Header API Key (Authorization)
                        .addSecuritySchemes("apiKey", createApiKeySecurityScheme())
                        // Cookie-based auth (optional)
                        .addSecuritySchemes("apiCookieKey", createCookieSecurityScheme())
                )
                // Security requirements applied globally
                .addSecurityItem(new SecurityRequirement().addList("oauth2", List.of("read", "trust", "write")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                .addSecurityItem(new SecurityRequirement().addList("apiCookieKey"));
    }

    @Bean
    public GroupedOpenApi allServiceApi() {
        return GroupedOpenApi.builder()
                .group("all-service-api")
                .packagesToScan("com.carbo")  // Scans all controllers in com.carbo package
                .build();
    }


    private SecurityScheme createOAuth2SecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .password(new OAuthFlow()
                                .tokenUrl(accessTokenUri)
                                .scopes(new Scopes()
                                        .addString("read", "read all"))));
//                                        .addString("trust", "trust all")
//                                        .addString("write", "write all"))));
    }

    private SecurityScheme createApiKeySecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Provide your API key or Bearer token here");
    }

    private SecurityScheme createCookieSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("Cookie")
                .description("Optional authentication via session cookie");
    }

    private Info apiInfo() {
        return new Info()
                .title("FracPro OPS API's for Jobs")
                .description("FracPro OPS API reference for developers")
                .termsOfService("https://ops.fracpro.ai")
                .contact(new Contact()
                        .name("FracPro OPS Software Development")
                        .url("https://ops.fracpro.ai")
                        .email("support@fracpro.com"))
                .license(new License()
                        .name("FracPro OPS License")
                        .url("https://ops.fracpro.ai"))
                .version("1.0");
    }
}
