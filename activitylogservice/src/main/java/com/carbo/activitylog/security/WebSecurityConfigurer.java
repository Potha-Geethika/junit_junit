package com.carbo.activitylog.security;

import com.carbo.activitylog.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer {

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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                                // Publicly accessible endpoints
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**",
                                        "/webjars/**",
                                        "/actuator/**"
                                ).permitAll()

                                // GET endpoints: accessible by multiple roles
                                .requestMatchers(HttpMethod.GET, "/**").hasAnyRole(Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE, Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK, Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT, Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.APP, Constants.SUPER_SALES_USER, Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD, Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR, Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY, Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG, Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG, Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE, Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE, Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER, Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW, Constants.SERVICEMANAGER)

                                // POST endpoints: same access as GET
                                .requestMatchers(HttpMethod.POST, "/**").hasAnyRole(Constants.USER)

                                // PUT endpoints restricted to USER role
                                .requestMatchers(HttpMethod.PUT, "/**").hasAnyRole(Constants.USER)

                                // DELETE endpoints restricted to USER role
                                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole(Constants.USER)

                                // All others must be authenticated
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(customJwtAuthenticationConverter)
                        )
                )
                .anonymous(anonymous -> anonymous.disable());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/actuator/health");
    }
}