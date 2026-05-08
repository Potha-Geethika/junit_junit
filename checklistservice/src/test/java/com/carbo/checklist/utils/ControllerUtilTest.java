package com.carbo.checklist.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.Map;
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
    void getOrganizationId_returnsOrganizationIdString() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationId", "org123");
        Mockito.when(token.getDetails()).thenReturn(details);
        Mockito.when(request.getUserPrincipal()).thenReturn(token);

        String orgId = ControllerUtil.getOrganizationId(request);

        assertNotNull(orgId);
        assertEquals("org123", orgId);
    }

    @Test
    void getOrganizationId_handlesOrganizationIdNotStringButHasToString() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Object nonStringOrgId = new Object() {
            @Override
            public String toString() {
                return "toStringId";
            }
        };
        Map<String, Object> details = new HashMap<>();
        details.put("organizationId", nonStringOrgId);
        Mockito.when(token.getDetails()).thenReturn(details);
        Mockito.when(request.getUserPrincipal()).thenReturn(token);

        String orgId = ControllerUtil.getOrganizationId(request);

        assertEquals("toStringId", orgId);
    }

    @Test
    void getOrganizationId_throwsClassCastExceptionIfPrincipalNotJwtAuthenticationToken() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(request.getUserPrincipal()).thenReturn(principal);

        assertThrows(ClassCastException.class, () -> ControllerUtil.getOrganizationId(request));
    }

    @Test
    void getOrganizationId_throwsNullPointerExceptionIfDetailsNull() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Mockito.when(token.getDetails()).thenReturn(null);
        Mockito.when(request.getUserPrincipal()).thenReturn(token);

        assertThrows(NullPointerException.class, () -> ControllerUtil.getOrganizationId(request));
    }

    @Test
    void getOrganizationId_throwsNullPointerExceptionIfOrganizationIdMissing() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        Mockito.when(token.getDetails()).thenReturn(details);
        Mockito.when(request.getUserPrincipal()).thenReturn(token);

        assertThrows(NullPointerException.class, () -> ControllerUtil.getOrganizationId(request));
    }
}