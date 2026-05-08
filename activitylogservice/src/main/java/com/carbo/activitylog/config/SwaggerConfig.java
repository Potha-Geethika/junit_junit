package com.carbo.activitylog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${config.oauth2.accessTokenUri}")
    private String accessTokenUri;

    @Value("${carbo.fracproop.service.baseurl}")
    private String baseUrl;

    // --- Replacements for apiKey() and apiCookieKey() ---
    private SecurityScheme apiKey() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(HttpHeaders.AUTHORIZATION);
    }

    private SecurityScheme apiCookieKey() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("apiKey");
    }

    // --- Equivalent to securitySchema() ---
    private SecurityScheme securitySchema() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .password(new OAuthFlow()
                                .tokenUrl(accessTokenUri)
                                .scopes(new Scopes()
                                        .addString("read", "read all")
                                        .addString("trust", "trust all")
                                        .addString("write", "write all"))));
    }

    // --- Equivalent to apiInfo() ---
    private Info apiInfo() {
        return new Info()
                .title("FracPro OPS API's for Activity Logs")
                .description("FracPro OPS API's reference for developers")
                .version("1.0")
                .termsOfService("https://ops.fracpro.ai")
                .contact(new Contact()
                        .name("FracPro OPS Software Development")
                        .url("https://ops.fracpro.ai")
                        .email("support@fracpro.com"))
                .license(new License()
                        .name("FracPro OPS License")
                        .url("support@fracpro.com"));
    }

    // --- Equivalent to your old Docket bean (kept name identical) ---
    @Bean
    public OpenAPI configureControllerPackageAndConvertors() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url(baseUrl+"activitylog/") // ✅ your external route
                ))
                .components(new Components()
                        .addSecuritySchemes("oauth2", securitySchema())
                        .addSecuritySchemes("apiKey", apiKey())
                        .addSecuritySchemes("apiCookieKey", apiCookieKey()))
                .addSecurityItem(new SecurityRequirement()
                        .addList("oauth2", List.of("read", "trust", "write")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                .addSecurityItem(new SecurityRequirement().addList("apiCookieKey"));
    }

    @Bean
    public GroupedOpenApi activitylogServiceApi() {
        return GroupedOpenApi.builder()
                .group("activitylog-service-api")
                .addOpenApiCustomizer(openApi ->
                        openApi.getPaths().entrySet().removeIf(e -> !e.getKey().startsWith("/v1/activity-logs/external")))
                .packagesToScan("com.carbo.activitylog.controllers.external")
                .build();
    }


    @Bean
    public GroupedOpenApi allServiceApi() {
        return GroupedOpenApi.builder()
                .group("all-service-api")
                .packagesToScan("com.carbo")  // Scans all controllers in com.carbo package
                .build();
    }
}
