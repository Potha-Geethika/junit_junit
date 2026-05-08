package com.carbo.checklist.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.Map;

public class ControllerUtil {
    public static String getOrganizationId(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return ((Map) ((JwtAuthenticationToken) principal).getDetails()).get("organizationId").toString();
    }
}
