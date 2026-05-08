package com.carbo.pad;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@SpringBootApplication
@CircuitBreaker
@RefreshScope
public class PadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PadServiceApplication.class, args);
    }

    @Bean
    public JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix(""); // No prefix if you don't want "ROLE_" by default
        return converter;
    }

}
