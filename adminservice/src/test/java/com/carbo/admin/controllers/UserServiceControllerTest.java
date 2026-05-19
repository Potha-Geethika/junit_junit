package com.carbo.admin.controllers;
import static org.mockito.ArgumentMatchers.anyString;
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
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private UserService userService;
    private DistrictService districtService;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        districtService = mock(DistrictService.class);
    }

    @Test
    public void testGetUsers_HappyPath() throws Exception {
        List<User> users = Arrays.asList(new User(), new User());
        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/v1/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetUser_HappyPath() throws Exception {
        String userId = "1";
        User user = new User();
        when(userService.getUser(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    public void testGetUser_NotFound() throws Exception {
        String userId = "1";
        when(userService.getUser(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/{userId}", userId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testUpdateLastPasswordResetDate_HappyPath() throws Exception {
        User user = new User();
        user.setUserName("testUser");
        when(userService.getUserByUserName(user.getUserName())).thenReturn(Optional.of(user));
        when(userService.saveUser(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/v1/users/updateLastPasswordResetDate")
                        .contentType("application/json")
                        .content("{\"userName\":\"testUser\", \"lastPassResetDate\":\"2022-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastPassResetDate").value("2022-01-01"));
    }

    @Test
    public void testUpdateLastPasswordResetDate_NotFound() throws Exception {
        User user = new User();
        user.setUserName("nonExistentUser");
        when(userService.getUserByUserName(user.getUserName())).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/users/updateLastPasswordResetDate")
                        .contentType("application/json")
                        .content("{\"userName\":\"nonExistentUser\", \"lastPassResetDate\":\"2022-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateSignature_HappyPath() throws Exception {
        ChangeSignature changeSignature = new ChangeSignature();
        changeSignature.setSignature("New Signature");
        User user = new User();
        user.setUserName("testUser");
        when(userService.getUserByUserName(user.getUserName())).thenReturn(Optional.of(user));

        mockMvc.perform(put("/v1/users/change-signature")
                        .contentType("application/json")
                        .content("{\"signature\":\"New Signature\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateSignature_UserNotFound() throws Exception {
        ChangeSignature changeSignature = new ChangeSignature();
        changeSignature.setSignature("New Signature");
        when(userService.getUserByUserName(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/users/change-signature")
                        .contentType("application/json")
                        .content("{\"signature\":\"New Signature\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSaveUser_HappyPath() throws Exception {
        User user = new User();
        user.setPassword("password");
        user.setUserName("newUser");
        when(userService.saveUserOnAzureAd(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/v1/users/")
                        .contentType("application/json")
                        .content("{\"userName\":\"newUser\", \"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testSaveUser_BadRequest() throws Exception {
        when(userService.saveUserOnAzureAd(any(User.class)))
                .thenThrow(new ErrorException(new Error("USER_ALREADY_EXISTS", "Username already exists.", HttpStatus.BAD_REQUEST)));

        mockMvc.perform(post("/v1/users/")
                        .contentType("application/json")
                        .content("{\"userName\":\"existingUser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteUser_HappyPath() throws Exception {
        String userId = "1";
        doNothing().when(userService).deleteUserOnAzureAd(userId);

        mockMvc.perform(delete("/v1/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testChangePassword_HappyPath() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        changePassword.setCurPassword("oldPassword");
        changePassword.setNewPassword("newPassword");
        when(userService.getUserByUserName(anyString())).thenReturn(Optional.of(new User()));

        mockMvc.perform(put("/v1/users/change-password")
                        .contentType("application/json")
                        .content("{\"curPassword\":\"oldPassword\", \"newPassword\":\"newPassword\", \"isStrength\":\"true\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testChangePassword_BadRequest() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        changePassword.setCurPassword("wrongPassword");
        changePassword.setNewPassword("newPassword");
        when(userService.getUserByUserName(anyString())).thenReturn(Optional.of(new User()));

        mockMvc.perform(put("/v1/users/change-password")
                        .contentType("application/json")
                        .content("{\"curPassword\":\"wrongPassword\", \"newPassword\":\"newPassword\", \"isStrength\":\"true\"}"))
                .andExpect(status().isBadRequest());
    }
}