package com.carbo.admin.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



class ControllerUtilTest {

    @Test
    void getUserName_returnsUserName() {
        HttpServletRequest request = createRequestWithDetail("userName", "testUser");
        String userName = ControllerUtil.getUserName(request);
        Assertions.assertEquals("testUser", userName);
    }

    @Test
    void getOrganizationId_returnsOrganizationId() {
        HttpServletRequest request = createRequestWithDetail("organizationId", "org123");
        String organizationId = ControllerUtil.getOrganizationId(request);
        Assertions.assertEquals("org123", organizationId);
    }

    @Test
    void getDistrictId_returnsDistrictIdPresent() {
        HttpServletRequest request = createRequestWithDetail("districtId", "districtX");
        Optional<String> districtId = ControllerUtil.getDistrictId(request);
        Assertions.assertTrue(districtId.isPresent());
        Assertions.assertEquals("districtX", districtId.get());
    }

    @Test
    void getDistrictId_returnsEmptyWhenDistrictIdMissing() {
        HttpServletRequest request = createRequestWithDetail("otherField", "value");
        Optional<String> districtId = ControllerUtil.getDistrictId(request);
        Assertions.assertTrue(districtId.isEmpty());
    }

    @Test
    void getRoles_returnsAuthorities() {
        HttpServletRequest request = createRequestWithDetail("authorities", "ROLE_ADMIN,ROLE_USER");
        String roles = ControllerUtil.getRoles(request);
        Assertions.assertEquals("ROLE_ADMIN,ROLE_USER", roles);
    }

    @Test
    void getUserName_throwsExceptionWhenUserNameMissing() {
        HttpServletRequest request = createRequestWithDetail("otherField", "value");
        Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            ControllerUtil.getUserName(request);
        });
    }

    @Test
    void getOrganizationId_throwsExceptionWhenOrganizationIdMissing() {
        HttpServletRequest request = createRequestWithDetail("otherField", "value");
        Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            ControllerUtil.getOrganizationId(request);
        });
    }

    @Test
    void getRoles_throwsExceptionWhenAuthoritiesMissing() {
        HttpServletRequest request = createRequestWithDetail("otherField", "value");
        Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            ControllerUtil.getRoles(request);
        });
    }

    @Test
    void extractFieldValueFromRequest_returnsEmptyOptionalWhenValueNull() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("userName", null);
        Mockito.when(token.getDetails()).thenReturn(details);
        Mockito.when(request.getUserPrincipal()).thenReturn(token);

        Optional<String> result = 
            (Optional<String>) 
            org.junit.platform.commons.util.ReflectionUtils.invokeMethod(
                ControllerUtil.class.getDeclaredMethod("extractFieldValueFromRequest", HttpServletRequest.class, String.class),
                null, request, "userName");

        Assertions.assertTrue(result.isEmpty());
    }

    private static HttpServletRequest createRequestWithDetail(String key, Object value) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put(key, value);
        Mockito.when(token.getDetails()).thenReturn(details);
        Mockito.when(request.getUserPrincipal()).thenReturn(token);
        return request;
    }
}