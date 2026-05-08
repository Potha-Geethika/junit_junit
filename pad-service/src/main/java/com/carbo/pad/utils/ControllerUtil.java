package com.carbo.pad.utils;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

public class ControllerUtil {
    public static String getOrganizationId(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return ((Map) ((JwtAuthenticationToken) principal).getDetails()).get("organizationId").toString();
    }
    public static String getOrganizationType(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return ((Map) ((JwtAuthenticationToken) principal).getDetails()).get("organizationType").toString();
    }
    public static String getOrganization(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return ((Map) ((JwtAuthenticationToken) principal).getDetails()).get("organization").toString();
    }
}
