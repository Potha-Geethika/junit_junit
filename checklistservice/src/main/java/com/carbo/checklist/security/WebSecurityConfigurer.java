package com.carbo.checklist.security;

import com.carbo.checklist.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfigurer{

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Autowired
    private CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole(Constants.USER, Constants.READ_ONLY, Constants.OPERATION)
                        .requestMatchers(HttpMethod.POST, "/**").hasAnyRole(Constants.ADMIN, Constants.BACK_OFFICE, Constants.OPERATION)
                        .requestMatchers(HttpMethod.PUT, "/**").hasAnyRole(Constants.ADMIN, Constants.BACK_OFFICE, Constants.OPERATION)
                        .requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole(Constants.ADMIN)
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated() // Require authentication for other requests
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            try {
                                jwt
                                        .decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(customJwtAuthenticationConverter);
                                // Use custom JWT decoder
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })


                )
                .anonymous(anonymous -> anonymous.disable());
        // Disable anonymous access

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/actuator/health"); // Ignore health endpoint
    }

}