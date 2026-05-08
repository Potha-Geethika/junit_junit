package com.carbo.admin.security;
import com.carbo.admin.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig{

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
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/**"
                        ).permitAll()
                        // Permit all for actuator health
                        .requestMatchers("/actuator/**").permitAll()

                        // Use regex to replace /**/change-password
                        .requestMatchers(new RegexRequestMatcher(".*/change-password", null)).permitAll()

                        // Use regex for lastPassResetDate anywhere in path
                        .requestMatchers(new RegexRequestMatcher(".*/lastPassResetDate/.*", null))
                        .hasAnyRole(
                                Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.APP,
                                Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE,
                                Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK,
                                Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT,
                                Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.SUPER_SALES_USER,
                                Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD,
                                Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR,
                                Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY,
                                Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG,
                                Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG,
                                Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE,
                                Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE,
                                Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER,
                                Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW,
                                Constants.SERVICEMANAGER
                        )

                        // Use regex for /**/change-signature
                        .requestMatchers(new RegexRequestMatcher(".*/change-signature", null))
                        .hasRole("USER")

                        // Specific HTTP methods for v1/users
                        .requestMatchers(HttpMethod.GET, "/v1/users/**")
                        .hasAnyRole(
                                Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.APP,
                                Constants.ORGANIZATION, Constants.ADMIN, Constants.CARBO_ADMIN, Constants.BACK_OFFICE,
                                Constants.SALES_USER, Constants.USER_MANAGEMENT, Constants.PRICEBOOK,
                                Constants.CREW_SCHEDULING, Constants.FIELDCOORDINATOR, Constants.PROCUREMENT,
                                Constants.SALES_FIELD_USER, Constants.MOVE_ONSITE_EQUIPMENT, Constants.SUPER_SALES_USER,
                                Constants.MS_ORGANIZATION, Constants.MS_ADMIN, Constants.MS_WELL, Constants.MS_PAD,
                                Constants.MS_OPERATOR, Constants.MS_DISTRICT, Constants.MS_FLEET, Constants.MS_VENDOR,
                                Constants.MS_EMAIL, Constants.MS_MISC_DATA, Constants.MS_SERVICE_COMPANY,
                                Constants.MS_JOB, Constants.MS_PUMP_ISSUE, Constants.MS_ACTIVITY_LOG,
                                Constants.MS_FIELD_TICKET, Constants.MS_ONSITE_EQUIPMENT, Constants.MS_CHANGE_LOG,
                                Constants.MS_PROPPANT_DELIVERY, Constants.MS_CHEMICAL_DELIVERY, Constants.MS_PROPPANT_STAGE,
                                Constants.MS_CHEMICAL_STAGE, Constants.MS_WS, Constants.MS_PUMP_SCHEDULE,
                                Constants.MS_WELL_INFO, Constants.MS_CHECKLIST, Constants.MS_WORKOVER,
                                Constants.MS_MAINTENANCE, Constants.MS_CONSUMABLE, Constants.MS_OPERATION_OVERVIEW,
                                Constants.SERVICEMANAGER
                        )
                        .requestMatchers(HttpMethod.POST, "/v1/users/**")
                        .hasAnyRole(Constants.USER, Constants.OPERATION, Constants.READ_ONLY, Constants.ADMIN, Constants.USER_MANAGEMENT)
                        .requestMatchers(HttpMethod.PUT, "/v1/users/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/v1/users/**").permitAll()

                        // Catch-all for remaining requests
                        .requestMatchers("/**").hasAnyRole("ADMIN", "USER_MANAGEMENT")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            try {
                                jwt.decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(customJwtAuthenticationConverter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                )
                .anonymous(anonymous -> anonymous.disable());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/v1/users/send-otp",
                        "/v1/users/validate-otp",
                        "/v1/user/migrate/getAllUsersForAi",
                        "/v1/user/migrate/saveAiUsers",
                        "/v1/user/migrate/setUserAzureId",
                        "/actuator/health"
                );
    }

}
