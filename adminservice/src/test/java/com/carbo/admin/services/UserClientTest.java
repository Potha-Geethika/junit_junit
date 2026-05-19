package com.carbo.admin.services;
import static org.mockito.ArgumentMatchers.any;

import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;






@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Value("${security.oauth2.resource.userInfoUri}")
    private String USER_API_URL = "http://localhost:8080/userinfo"; // Dummy value for testing

    @InjectMocks
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        userClient = new UserClient();
    }

    @Test
    void testGetUserInfo_HappyPath() {
        String accessToken = "valid-token";
        Map<String, Object> expectedResponse = Map.of("id", 1, "name", "Test User");

        ResponseEntity<Map> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(expectedResponse);
        when(restTemplate.exchange(eq(USER_API_URL), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void testGetUserInfo_EmptyResponse() {
        String accessToken = "valid-token";

        ResponseEntity<Map> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(Collections.emptyMap());
        when(restTemplate.exchange(eq(USER_API_URL), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    void testGetUserInfo_NullResponse() {
        String accessToken = "valid-token";

        ResponseEntity<Map> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(null);
        when(restTemplate.exchange(eq(USER_API_URL), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        assertNotNull(result);
        assertEquals(null, result);
    }

    @Test
    void testGetUserInfo_RuntimeException() {
        String accessToken = "valid-token";

        when(restTemplate.exchange(eq(USER_API_URL), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Error occurred"));

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        assertNotNull(result);
    }

    @Test
    void testGetUserInfo_IOException() {
        String accessToken = "valid-token";

        when(restTemplate.exchange(eq(USER_API_URL), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(new IOException("IO Error occurred"));

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        assertNotNull(result);
    }

    @Test
    void testGetUserInfo_IllegalArgumentException() {
        String accessToken = null;

        try {
            userClient.getUserInfo(accessToken);
        } catch (IllegalArgumentException e) {
            assertEquals("Access token cannot be null", e.getMessage());
        }
    }
}