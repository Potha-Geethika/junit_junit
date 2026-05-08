package com.carbo.activitylog.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class UserClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${security.oauth2.resource.userInfoUri}")
    private String USER_API_URL;

    public Map<String, Object> getUserInfo(String accessToken) {
        // Set up the headers with the Bearer token (OAuth2 token)
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);  // Use the access token

        // Create the HTTP request entity
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Make the REST call
        ResponseEntity<Map> response = restTemplate.exchange(
                USER_API_URL,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        // Return the response body (user information)
        return response.getBody();
    }
}