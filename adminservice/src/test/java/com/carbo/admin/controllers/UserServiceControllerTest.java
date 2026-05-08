package com.carbo.admin.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.model.*;
import com.carbo.admin.model.Error;
import com.carbo.admin.services.DistrictService;
import com.carbo.admin.services.UserService;
import com.carbo.admin.utils.Constants;
import com.carbo.admin.utils.ControllerUtil;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.mongodb.MongoWriteException;
import io.netty.handler.ssl.SslContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.time.Instant;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import static com.carbo.admin.utils.Constants.*;
import static com.carbo.admin.utils.Constants.USER_UPDATE_MESSAGE;
import static com.carbo.admin.utils.ControllerUtil.getUserName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.passay.CharacterCharacteristicsRule.ERROR_CODE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;







@WebMvcTest(UserServiceController.class)
class UserServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private DistrictService districtService;

    @MockBean
    private SslContext sslContext;

    @MockBean
    private WebClient webClient;

    @MockBean
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    // Helper method to create a sample User
    private User sampleUser() {
        User user = new User();
        user.setId("user123");
        user.setUserName("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAzureId("azureId123");
        user.setLastPassResetDate(new Date());
        user.setLastFivePasswords(new ArrayList<>());
        user.setStrength(true);
        user.setDistrictId("district1");
        user.setDistrictids(new ArrayList<>(List.of("districtOld")));
        user.setOrganizationId("org1");
        return user;
    }

    // Helper method to create sample list of Users
    private List<User> sampleUserList() {
        return List.of(sampleUser());
    }

    // Helper method to create a sample ChangeSignature
    private ChangeSignature sampleChangeSignature() {
        ChangeSignature cs = new ChangeSignature();
        cs.setSignature("NewSignature");
        return cs;
    }

    // Helper method to create a sample ChangePassword
    private ChangePassword sampleChangePassword() {
        ChangePassword cp = new ChangePassword();
        cp.setCurPassword("OldPass123!");
        cp.setNewPassword("NewPass123!");
        cp.setIsStrength("true");
        return cp;
    }

    // Helper method to create a sample UserPatchDTO
    private UserPatchDTO sampleUserPatchDTO() {
        UserPatchDTO dto = new UserPatchDTO();
        dto.setUserId("user123");
        dto.setSelectedColumns(List.of("col1", "col2"));
        return dto;
    }

    // Helper method to create sample filters map
    private Map<String,Object> sampleFilters() {
        Map<String,Object> filters = new HashMap<>();
        filters.put("filterKey", "filterValue");
        return filters;
    }

    // Mock HttpServletRequest with required headers or principal
    private HttpServletRequest mockRequestWithPrincipal(String username, boolean isCarboAdmin) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        // Mock principal and authorities
        java.security.Principal principal = mock(java.security.Principal.class);
        when(request.getUserPrincipal()).thenReturn(principal);

        org.springframework.security.authentication.AbstractAuthenticationToken authToken = mock(org.springframework.security.authentication.AbstractAuthenticationToken.class);
        when(request.getUserPrincipal()).thenReturn(authToken);
        if (isCarboAdmin) {
            when(authToken.getAuthorities()).thenReturn(
                    Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CARBO_ADMIN")));
        } else {
            when(authToken.getAuthorities()).thenReturn(Collections.emptyList());
        }
        return request;
    }

    @Nested
    @DisplayName("getUsers Tests")
    class GetUsersTests {

        @Test
        @DisplayName("GET /v1/users/ - CARBO_ADMIN role returns all users with 200")
        void getUsers_CarboAdmin_ReturnsAllUsers() throws Exception {
            List<User> users = sampleUserList();
            when(userService.getAll()).thenReturn(users);

            HttpServletRequest request = mockRequestWithPrincipal("admin@carbo.com", true);

            mockMvc.perform(get("/v1/users/")
                    .principal(request.getUserPrincipal()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(users.size())))
                    .andExpect(jsonPath("$[0].userName", is(users.get(0).getUserName())));

            verify(userService, times(1)).getAll();
            verify(userService, never()).getByOrganizationId(any());
        }

        @Test
        @DisplayName("GET /v1/users/ - Non CARBO_ADMIN with organizationId returns users by organization")
        void getUsers_NonCarboAdminWithOrgId_ReturnsUsersByOrg() throws Exception {
            List<User> users = sampleUserList();
            when(userService.getByOrganizationId(any())).thenReturn(users);

            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getUserPrincipal()).thenReturn(mock(org.springframework.security.authentication.AbstractAuthenticationToken.class));
            // Mock ControllerUtil.getOrganizationId(request) static call using Mockito.mockStatic
            try (var mockedStatic = org.mockito.Mockito.mockStatic(com.carbo.admin.utils.ControllerUtil.class)) {
                mockedStatic.when(() -> com.carbo.admin.utils.ControllerUtil.getOrganizationId(request)).thenReturn("org1");
                mockedStatic.when(() -> com.carbo.admin.utils.ControllerUtil.getUserName(request)).thenReturn("user@domain.com");

                mockMvc.perform(get("/v1/users/")
                        .principal(request.getUserPrincipal()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(users.size())))
                        .andExpect(jsonPath("$[0].userName", is(users.get(0).getUserName())));

                verify(userService, times(1)).getByOrganizationId("org1");
                verify(userService, never()).getAll();
            }
        }

        @Test
        @DisplayName("GET /v1/users/ - Non CARBO_ADMIN with NullPointerException returns all users for specific user")
        void getUsers_NonCarboAdmin_NullPointerException_ReturnsAllUsersForSpecificUser() throws Exception {
            List<User> users = sampleUserList();
            when(userService.getAll()).thenReturn(users);

            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getUserPrincipal()).thenReturn(mock(org.springframework.security.authentication.AbstractAuthenticationToken.class));
            try (var mockedStatic = org.mockito.Mockito.mockStatic(com.carbo.admin.utils.ControllerUtil.class)) {
                mockedStatic.when(() -> com.carbo.admin.utils.ControllerUtil.getOrganizationId(request)).thenThrow(NullPointerException.class);
                mockedStatic.when(() -> com.carbo.admin.utils.ControllerUtil.getUserName(request)).thenReturn("duy.nguyen@carboceramics.com");

                mockMvc.perform(get("/v1/users/")
                        .principal(request.getUserPrincipal()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(users.size())))
                        .andExpect(jsonPath("$[0].userName", is(users.get(0).getUserName())));

                verify(userService, times(1)).getAll();
                verify(userService, never()).getByOrganizationId(any());
            }
        }
    }

    @Nested
    @DisplayName("getUser Tests")
    class GetUserTests {

        @Test
        @DisplayName("GET /v1/users/{userId} - Happy path returns user and 200")
        void getUser_HappyPath_ReturnsUser() throws Exception {
            User user = sampleUser();
            when(userService.getUser(any())).thenReturn(Optional.of(user));

            mockMvc.perform(get("/v1/users/{userId}", "user123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userName", is(user.getUserName())))
                    .andExpect(jsonPath("$.id", is(user.getId())));

            verify(userService).getUser("user123");
        }

        @Test
        @DisplayName("GET /v1/users/{userId} - Service returns empty optional leads to 500")
        void getUser_EmptyOptional_ThrowsException() throws Exception {
            when(userService.getUser(any())).thenReturn(Optional.empty());

            mockMvc.perform(get("/v1/users/{userId}", "user123"))
                    .andExpect(status().isInternalServerError());

            verify(userService).getUser("user123");
        }
    }

    @Nested
    @DisplayName("getUserLastPassResetDate Tests")
    class GetUserLastPassResetDateTests {

        @Test
        @DisplayName("GET /v1/users/lastPassResetDate/{userName} - Happy path returns date and 200")
        void getUserLastPassResetDate_HappyPath_ReturnsDate() throws Exception {
            User user = sampleUser();
            user.setLastPassResetDate(new Date(123456789L));
            when(userService.getUserByUserName(any())).thenReturn(Optional.of(user));

            mockMvc.perform(get("/v1/users/lastPassResetDate/{userName}", "john.doe@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", notNullValue()));

            verify(userService).getUserByUserName("john.doe@example.com");
        }

        @Test
        @DisplayName("GET /v1/users/lastPassResetDate/{userName} - Service returns empty optional leads to 500")
        void getUserLastPassResetDate_EmptyOptional_ThrowsException() throws Exception {
            when(userService.getUserByUserName(any())).thenReturn(Optional.empty());

            mockMvc.perform(get("/v1/users/lastPassResetDate/{userName}", "john.doe@example.com"))
                    .andExpect(status().isInternalServerError());

            verify(userService).getUserByUserName("john.doe@example.com");
        }
    }

    @Nested
    @DisplayName("saveLastPassResetDate Tests")
    class SaveLastPassResetDateTests {

        @Test
        @DisplayName("POST /v1/users/lastPassResetDate/{userName} - Saves lastPassResetDate (void method)")
        void saveLastPassResetDate_HappyPath() throws Exception {
            User userFromRequest = sampleUser();
            userFromRequest.setLastPassResetDate(new Date(1111L));
            User existUser = sampleUser();
            when(userService.getUserByUserName(any())).thenReturn(Optional.of(existUser));
            doNothing().when(userService).saveUser(any());

            String jsonBody = "{\"lastPassResetDate\":\"2020-01-01T00:00:00.000+00:00\",\"strength\":true}";

            mockMvc.perform(post("/v1/users/lastPassResetDate/{userName}", "john.doe@example.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isOk());

            verify(userService).getUserByUserName("john.doe@example.com");
            verify(userService).saveUser(any(User.class));
        }

        @Test
        @DisplayName("POST /v1/users/lastPassResetDate/{userName} - Service returns empty optional leads to 500")
        void saveLastPassResetDate_EmptyOptional_ThrowsException() throws Exception {
            when(userService.getUserByUserName(any())).thenReturn(Optional.empty());

            String jsonBody = "{\"lastPassResetDate\":\"2020-01-01T00:00:00.000+00:00\",\"strength\":true}";

            mockMvc.perform(post("/v1/users/lastPassResetDate/{userName}", "john.doe@example.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isInternalServerError());

            verify(userService).getUserByUserName("john.doe@example.com");
            verify(userService, never()).saveUser(any());
        }
    }

    @Nested
    @DisplayName("updateLastPasswordResetDate Tests")
    class UpdateLastPasswordResetDateTests {

        @Test
        @DisplayName("POST /v1/users/updateLastPasswordResetDate - Happy path returns updated User and 200")
        void updateLastPasswordResetDate_HappyPath() throws Exception {
            User existUser = sampleUser();
            User savedUser = sampleUser();
            savedUser.setLastPassResetDate(new Date());

            when(userService.getUserByUserName(any())).thenReturn(Optional.of(existUser));
            when(userService.saveUser(any())).thenReturn(savedUser);

            String jsonBody = "{\"userName\":\"john.doe@example.com\"}";

            mockMvc.perform(post("/v1/users/updateLastPasswordResetDate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userName\":\"john.doe@example.com\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userName", is(existUser.getUserName())));

            verify(userService).getUserByUserName("john.doe@example.com");
            verify(userService).updateLastPasswordResetFlagOnAzure(existUser.getAzureId());
            verify(userService).saveUser(existUser);
        }

        @Test
        @DisplayName("POST /v1/users/updateLastPasswordResetDate - User not found leads to 500")
        void updateLastPasswordResetDate_UserNotFound_ThrowsException() throws Exception {
            when(userService.getUserByUserName(any())).thenReturn(Optional.empty());

            mockMvc.perform(post("/v1/users/updateLastPasswordResetDate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userName\":\"john.doe@example.com\"}"))
                    .andExpect(status().isInternalServerError());

            verify(userService).getUserByUserName("john.doe@example.com");
            verify(userService, never()).updateLastPasswordResetFlagOnAzure(any());
            verify(userService, never()).saveUser(any());
        }
    }

    @Nested
    @DisplayName("updateSignature Tests")
    class UpdateSignatureTests {

        @Test
        @DisplayName("PUT /v1/users/change-signature - Happy path returns 200")
        void updateSignature_HappyPath() throws Exception {
            User existUser = sampleUser();
            when(userService.getUserByUserName(any())).thenReturn(Optional.of(existUser));
            doNothing().when(userService).updateUser(any());

            String jsonBody = "{\"signature\":\"NewSignature\"}";

            HttpServletRequest request = mock(HttpServletRequest.class);
            try (var mockedStatic = Mockito.mockStatic(com.carbo.admin.utils.ControllerUtil.class)) {
                mockedStatic.when(() -> com.carbo.admin.utils.ControllerUtil.getUserName(request)).thenReturn("john.doe@example.com");

                mockMvc.perform(put("/v1/users/change-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .header("Authorization", "token")
                        .requestAttr(HttpServletRequest.class.getName(), request))
                        .andExpect(status().isOk());
            }

            verify(userService).getUserByUserName(any());
            verify(userService).updateUser(any());
        }

        @Test
        @DisplayName("PUT /v1/users/change-signature - User not found returns 400")
        void updateSignature_UserNotFound_ReturnsBadRequest() throws Exception {
            when(userService.getUserByUserName(any())).thenReturn(Optional.empty());

            String jsonBody = "{\"signature\":\"NewSignature\"}";

            HttpServletRequest request = mock(HttpServletRequest.class);
            try (var mockedStatic = Mockito.mockStatic(com.carbo.admin.utils.ControllerUtil.class)) {
                mockedStatic.when(() -> com.carbo.admin.utils.ControllerUtil.getUserName(request)).thenReturn("john.doe@example.com");

                mockMvc.perform(put("/v1/users/change-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .requestAttr(HttpServletRequest.class.getName(), request))
                        .andExpect(status().isBadRequest());
            }

            verify(userService).getUserByUserName(any());
            verify(userService, never()).updateUser(any());
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("PUT /v1/users/{userId} - isLogin true calls saveUser and returns 200 with response body")
        void updateUser_isLoginTrue() throws Exception {
            User existingUser = sampleUser();
            when(userService.getUser(any())).thenReturn(Optional.of(existingUser));
            when(userService.saveUser(any())).thenReturn(existingUser);

            String jsonBody = "{\"userName\":\"john.doe@example.com\"}";

            mockMvc.perform(put("/v1/users/{userId}?isLogin=true", "user123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode", is("USER_UPDATE_CODE")))
                    .andExpect(jsonPath("$.successMessage", is("update user login time")));

            verify(userService).getUser("user123");
            verify(userService).saveUser(existingUser);
            verify(userService, never()).updateUserOnAzureAd(any(), any());
        }

        @Test
        @DisplayName("PUT /v1/users/{userId} - isLogin false calls updateUserOnAzureAd and returns 200 with response body")
        void updateUser_isLoginFalse() throws Exception {
            User existingUser = sampleUser();
            when(userService.getUser(any())).thenReturn(Optional.of(existingUser));
            doNothing().when(userService).updateUserOnAzureAd(any(), any());

            String jsonBody = "{\"userName\":\"john.doe@example.com\"}";

            mockMvc.perform(put("/v1/users/{userId}", "user123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode", is("USER_UPDATE_CODE")))
                    .andExpect(jsonPath("$.successMessage", is("User Update Successfully")));

            verify(userService).getUser("user123");
            verify(userService).updateUserOnAzureAd(any(), eq("user123"));
            verify(userService, never()).saveUser(any());
        }

        @Test
        @DisplayName("PUT /v1/users/{userId} - Service returns empty optional leads to 500")
        void updateUser_UserNotFound_ThrowsException() throws Exception {
            when(userService.getUser(any())).thenReturn(Optional.empty());

            String jsonBody = "{\"userName\":\"john.doe@example.com\"}";

            mockMvc.perform(put("/v1/users/{userId}", "user123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isInternalServerError());

            verify(userService).getUser("user123");
            verify(userService, never()).updateUserOnAzureAd(any(), any());
        }
    }

    @Nested
    @DisplayName("saveUser Tests")
    class SaveUserTests {

        @Test
        @DisplayName("POST /v1/users/ - Happy path returns 200 with success response")
        void saveUser_HappyPath() throws Exception {
            User user = sampleUser();
            Response response = Response.builder().successCode("USER_CREATED_CODE").successMessage("User created Successfully").build();

            when(userService.saveUserOnAzureAd(any())).thenReturn(new UserResponse("msg", HttpStatus.CREATED, user));
            // We cannot mock sendEmail (private), so just test controller behavior

            String jsonBody = "{\"userName\":\"john.doe@example.com\"}";

            mockMvc.perform(post("/v1/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode", is("USER_CREATED_CODE")))
                    .andExpect(jsonPath("$.successMessage", is("User created Successfully")));

            verify(userService).saveUserOnAzureAd(any());
        }

        @Test
        @DisplayName("POST /v1/users/ - DuplicateKeyException returns 400 with error body")
        void saveUser_DuplicateKeyException_ReturnsBadRequest() throws Exception {
            when(userService.saveUserOnAzureAd(any())).thenThrow(new org.springframework.dao.DuplicateKeyException("Duplicate key"));

            String jsonBody = "{\"userName\":\"john.doe@example.com\"}";

            mockMvc.perform(post("/v1/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("USER_ALREADY_EXISTS")))
                    .andExpect(jsonPath("$.errorMessage", is("Username already exists.")));

            verify(userService).saveUserOnAzureAd(any());
        }

        @Test
        @DisplayName("POST /v1/users/ - Other Exception returns 500 with error message")
        void saveUser_OtherException_ReturnsInternalServerError() throws Exception {
            when(userService.saveUserOnAzureAd(any())).thenThrow(new RuntimeException("Some error"));

            String jsonBody = "{\"userName\":\"john.doe@example.com\"}";

            mockMvc.perform(post("/v1/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("Error:")));

            verify(userService).saveUserOnAzureAd(any());
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("DELETE /v1/users/{userId} - Calls deleteUserOnAzureAd and returns 204")
        void deleteUser_HappyPath() throws Exception {
            doNothing().when(userService).deleteUserOnAzureAd(any());

            mockMvc.perform(delete("/v1/users/{userId}", "user123"))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUserOnAzureAd("user123");
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("PUT /v1/users/change-password - Happy path returns 200")
        void changePassword_HappyPath() throws Exception {
            User existUser = sampleUser();
            existUser.setPassword(new BCryptPasswordEncoder(12).encode("OldPass123!"));
            existUser.setLastFivePasswords(new ArrayList<>());
            when(userService.getUserByUserName(any())).thenReturn(Optional.of(existUser));
            doNothing().when(userService).updateUser(any());

            ChangePassword cp = sampleChangePassword();
            String jsonBody = "{\"curPassword\":\"OldPass123!\",\"newPassword\":\"NewPass123!\",\"isStrength\":\"true\"}";

            HttpServletRequest request = mock(HttpServletRequest.class);
            try (var mockedStatic = Mockito.mockStatic(com.carbo.admin.utils.ControllerUtil.class)) {
                mockedStatic.when(() -> com.carbo.admin.utils.ControllerUtil.getUserName(request)).thenReturn("john.doe@example.com");

                mockMvc.perform(put("/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .requestAttr(HttpServletRequest.class.getName(), request))
                        .andExpect(status().isOk());
            }

            verify(userService).getUserByUserName(any());
            verify(userService).updateUser(any());
        }

        @Test
        @DisplayName("PUT /v1/users/change-password - Current password mismatch returns 400")
        void changePassword_CurrentPasswordMismatch() throws Exception {
            User existUser = sampleUser();
            existUser.setPassword(new BCryptPasswordEncoder(12).encode("DifferentOldPass!"));
            when(userService.getUserByUserName(any())).thenReturn(Optional.of(existUser));

            String jsonBody = "{\"curPassword\":\"WrongPass!\",\"newPassword\":\"NewPass123!\",\"isStrength\":\"true\"}";

            HttpServletRequest request = mock(HttpServletRequest.class);
            try (var mockedStatic = Mockito.mockStatic(com.carbo.admin.utils.ControllerUtil.class)) {
                mockedStatic.when(() -> com.carbo.admin.utils.ControllerUtil.getUserName(request)).thenReturn("john.doe@example.com");

                mockMvc.perform(put("/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .requestAttr(HttpServletRequest.class.getName(), request))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string(containsString("Current Password doesn't match")));
            }

            verify(userService).getUserByUserName(any());
            verify(userService, never()).updateUser(any());
        }

        @Test
        @DisplayName("PUT /v1/users/change-password - Null curPassword returns 400")
        void changePassword_NullCurPassword() throws Exception {
            String jsonBody = "{\"newPassword\":\"NewPass123!\",\"isStrength\":\"true\"}";

            mockMvc.perform(put("/v1/users/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).getUserByUserName(any());
            verify(userService, never()).updateUser(any());
        }
    }

    @Nested
    @DisplayName("updateDistrictIds Tests")
    class UpdateDistrictIdsTests {

        @Test
        @DisplayName("PUT /v1/users/updateDistrictIds - Updates districtIds and calls updateUser")
        void updateDistrictIds_HappyPath() throws Exception {
            User user = sampleUser();
            List<User> users = List.of(user);

            List<District> districts = List.of(new District());
            districts.get(0).setId("district1");

            when(userService.getAll()).thenReturn(users);
            when(districtService.getByOrganizationId(any())).thenReturn(districts);
            doNothing().when(userService).updateUser(any());

            mockMvc.perform(put("/v1/users/updateDistrictIds"))
                    .andExpect(status().isOk());

            verify(userService).getAll();
            verify(districtService).getByOrganizationId(user.getOrganizationId());
            verify(userService).updateUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("updateColumns Tests")
    class UpdateColumnsTests {

        @Test
        @DisplayName("POST /v1/users/update-columns - Calls updateSelectColumn")
        void updateColumns_HappyPath() throws Exception {
            doNothing().when(userService).updateSelectColumn(any());

            String jsonBody = "{\"userId\":\"user123\",\"selectedColumns\":[\"col1\",\"col2\"]}";

            mockMvc.perform(post("/v1/users/update-columns")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isOk());

            verify(userService).updateSelectColumn(any());
        }
    }

    @Nested
    @DisplayName("updateUserFilters Tests")
    class UpdateUserFiltersTests {

        @Test
        @DisplayName("PATCH /v1/users/{userId}/filters - Happy path returns updated User with 200")
        void updateUserFilters_HappyPath() throws Exception {
            User updatedUser = sampleUser();
            Map<String,Object> filters = sampleFilters();

            when(userService.updateUserFilters(any(), any())).thenReturn(updatedUser);

            String jsonBody = "{\"filterKey\":\"filterValue\"}";

            mockMvc.perform(patch("/v1/users/{userId}/filters", "user123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userName", is(updatedUser.getUserName())));

            verify(userService).updateUserFilters("user123", filters);
        }

        @Test
        @DisplayName("PATCH /v1/users/{userId}/filters - Service throws exception leads to 500")
        void updateUserFilters_ServiceException_Returns500() throws Exception {
            when(userService.updateUserFilters(any(), any())).thenThrow(new RuntimeException("DB error"));

            String jsonBody = "{\"filterKey\":\"filterValue\"}";

            mockMvc.perform(patch("/v1/users/{userId}/filters", "user123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isInternalServerError());

            verify(userService).updateUserFilters(any(), any());
        }
    }
}