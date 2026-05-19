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





public class ControllerUtilTest {

    @Test
    void testGetUserName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("userName", "testUser");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);
        
        String userName = ControllerUtil.getUserName(request);
        Assertions.assertEquals("testUser", userName);
    }

    @Test
    void testGetOrganizationId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationId", "org123");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);
        
        String organizationId = ControllerUtil.getOrganizationId(request);
        Assertions.assertEquals("org123", organizationId);
    }

    @Test
    void testGetDistrictIdPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("districtId", "dist456");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);
        
        Optional<String> districtId = ControllerUtil.getDistrictId(request);
        Assertions.assertTrue(districtId.isPresent());
        Assertions.assertEquals("dist456", districtId.get());
    }

    @Test
    void testGetDistrictIdAbsent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);
        
        Optional<String> districtId = ControllerUtil.getDistrictId(request);
        Assertions.assertFalse(districtId.isPresent());
    }

    @Test
    void testGetRoles() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("authorities", "ROLE_USER");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);
        
        String roles = ControllerUtil.getRoles(request);
        Assertions.assertEquals("ROLE_USER", roles);
    }
}