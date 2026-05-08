package com.carbo.admin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.client.RestTemplate;

public class CustomJwtDecoder implements JwtDecoder {
    private static final Logger logger = LoggerFactory.getLogger(CustomJwtDecoder.class);
    private final JwtDecoder delegate;
    @Autowired
    private RestTemplate restTemplate;

    public CustomJwtDecoder(JwtDecoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        // Log the token before decoding
        logger.debug("Received token: {}", token);

        Jwt jwt = delegate.decode(token);

        // Log the decoded JWT and its claims
        logger.debug("Decoded JWT: {}", jwt);
        logger.debug("JWT Claims: {}", jwt.getClaims());
        return jwt;
    }
}

