package com.carbo.admin.utils;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

public class ControllerUtil {
    public static String getUserName(HttpServletRequest request) {
        return extractFieldValueFromRequest(request, "userName").get();
    }

    public static String getOrganizationId(HttpServletRequest request) {
        return extractFieldValueFromRequest(request, "organizationId").get();
    }

    public static Optional<String> getDistrictId(HttpServletRequest request) {
        return extractFieldValueFromRequest(request, "districtId");
    }
    public static String getRoles(HttpServletRequest request) {
        return extractFieldValueFromRequest(request, "authorities").get();
    }


    private static Optional<String> extractFieldValueFromRequest(HttpServletRequest request, String fieldName) {
        Principal principal = request.getUserPrincipal();
        Object value = ((Map) ((JwtAuthenticationToken) principal).getDetails()).get(fieldName);
        return value == null ? Optional.empty() : Optional.of(value.toString());
    }
}
