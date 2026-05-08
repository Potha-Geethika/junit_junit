package com.carbo.admin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.carbo.admin.services.UserClient;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract roles from the token
        Collection<GrantedAuthority> authorities = new HashSet<>(jwtGrantedAuthoritiesConverter.convert(jwt));

        Map user = userClient.getUserInfo(jwt.getTokenValue());
        List<String> listAuthorities = ((List) user.get("authorities"));
        authorities.addAll(listAuthorities.stream().map(role -> new SimpleGrantedAuthority(role)) // Assuming Role has a getName method
                .collect(Collectors.toList()));

        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities);
        authenticationToken.setDetails(user); // Set custom user details

        // Return a new AuthenticationToken object with the combined authorities
        return authenticationToken;
    }
}
