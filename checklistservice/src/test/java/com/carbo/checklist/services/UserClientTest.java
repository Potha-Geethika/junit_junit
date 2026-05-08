package com.carbo.checklist.services;
import static org.mockito.ArgumentMatchers.anyString;
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

    private UserClient userClient;

    private static final String TEST_TOKEN = "test-token";
    private static final String TEST_URL = "http://test-url";

    @BeforeEach
    void setUp() throws Exception {
        userClient = new UserClient();
        // Inject mocked RestTemplate via reflection since no setter or constructor
        var restTemplateField = UserClient.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(userClient, restTemplate);
        // Inject USER_API_URL string value via reflection
        var urlField = UserClient.class.getDeclaredField("USER_API_URL");
        urlField.setAccessible(true);
        urlField.set(userClient, TEST_URL);
    }

    @Test
    void getUserInfo_HappyPath_ReturnsMap() {
        Map<String, Object> expectedMap = Collections.singletonMap("key", "value");
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(expectedMap);

        when(restTemplate.exchange(
                eq(TEST_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(TEST_TOKEN);

        assertNotNull(result);
        assertEquals(expectedMap, result);

        // Verify that the headers contain the Bearer token
        verify(restTemplate).exchange(eq(TEST_URL), eq(HttpMethod.GET), 
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
                    return auth != null && auth.equals("Bearer " + TEST_TOKEN);
                }), eq(Map.class));
    }

    @Test
    void getUserInfo_EmptyResponseBody_ReturnsNull() {
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                eq(TEST_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(TEST_TOKEN);

        assertNull(result);
    }

    @Test
    void getUserInfo_NullAccessToken_StillSetsHeader() {
        Map<String, Object> expectedMap = Collections.singletonMap("key", "value");
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(expectedMap);

        when(restTemplate.exchange(
                eq(TEST_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(null);

        assertNotNull(result);
        assertEquals(expectedMap, result);

        verify(restTemplate).exchange(eq(TEST_URL), eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
                    // If token null, header is "Bearer null" (per HttpHeaders.setBearerAuth impl)
                    return auth != null && auth.equals("Bearer null");
                }), eq(Map.class));
    }

    @Test
    void getUserInfo_RestTemplateThrowsRuntimeException_Propagates() {
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RuntimeException("Error"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userClient.getUserInfo(TEST_TOKEN));
        assertEquals("Error", thrown.getMessage());
    }

    @Test
    void getUserInfo_RestTemplateThrowsException_Propagates() {
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new IllegalArgumentException("Bad arg"));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userClient.getUserInfo(TEST_TOKEN));
        assertEquals("Bad arg", thrown.getMessage());
    }
}