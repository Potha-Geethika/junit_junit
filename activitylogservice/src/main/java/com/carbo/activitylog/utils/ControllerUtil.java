package com.carbo.activitylog.utils;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

public class ControllerUtil {
    public static String getOrganizationId(HttpServletRequest request) {
        return extractFieldValueFromRequest(request, "organizationId").get();
    }

    public static String getUserFullName(HttpServletRequest request) {
        return extractFieldValueFromRequest(request, "fullName").get();
    }

    private static Optional<String> extractFieldValueFromRequest(HttpServletRequest request, String fieldName) {
        Principal principal = request.getUserPrincipal();

        if (principal instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken oauth2AuthToken = (JwtAuthenticationToken) principal;
            Map<String, Object> details = (Map<String, Object>) oauth2AuthToken.getDetails();
            Object value = details.get(fieldName);
            return value == null ? Optional.empty() : Optional.of(value.toString());
        }

        return Optional.empty(); // Return empty if not an OAuth2AuthenticationToken
    }

    public static Integer getCurDay(Long jobStartDate, ZoneId zone) {
        ZonedDateTime previousDate = Instant.now().atZone(zone).minusDays(1);
        LocalDate startDate = Instant.ofEpochMilli(jobStartDate).atZone(zone).toLocalDate();
        //calculating number of days in between
        long noOfDaysBetween = ChronoUnit.DAYS.between(startDate, previousDate);
        return (int) noOfDaysBetween + 1;
    }

    public static String getOrganizationType(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return ((Map) ((JwtAuthenticationToken) principal).getDetails()).get("organizationType").toString();
    }

    public static String getOrganizationName(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();

        if (principal instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken oauth2AuthToken = (JwtAuthenticationToken) principal;
            Map<String, Object> details = (Map<String, Object>) oauth2AuthToken.getDetails();
            Object value = details.get("organizationName");

            return value != null ? value.toString() : ""; // Return empty string if the value is null
        }

        return ""; // Return empty string if the principal is not OAuth2AuthenticationToken
    }

    public static String getUserName(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();

        if (principal instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken oauth2AuthToken = (JwtAuthenticationToken) principal;
            Map<String, Object> details = (Map<String, Object>) oauth2AuthToken.getDetails();
            Object value = details.get("userName");

            return value != null ? value.toString() : ""; // Return empty string if the value is null
        }

        return ""; // Return empty string if the principal is not OAuth2AuthenticationToken
    }
}
