package com.carbo.admin.services;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.kafka.Producer;
import com.carbo.admin.model.*;
import com.carbo.admin.model.Error;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponse;
import com.carbo.admin.repository.UserMongoDbRepository;
import com.carbo.admin.utils.Constants;
import com.microsoft.graph.models.ObjectIdentity;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;
import java.util.stream.Collectors;
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
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import static com.carbo.admin.utils.Constants.INVALID_OTP_CODE;
import static com.carbo.admin.utils.Constants.OTP_EXPIRED_CODE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;






@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMongoDbRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private Producer producer;

    @Mock
    private GraphServiceClient graphServiceClient;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserName("testuser");
        user.setPassword("Password123!");
        user.setFirstName("Test");
        user.setLastName("User");
    }

    @Test
    void getAll() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        assertNotNull(userService.getAll());
        assertEquals(1, userService.getAll().size());
    }

    @Test
    void getByOrganizationId() {
        when(userRepository.findByOrganizationId(anyString())).thenReturn(Collections.singletonList(user));
        assertNotNull(userService.getByOrganizationId("orgId"));
        assertEquals(1, userService.getByOrganizationId("orgId").size());
    }

    @Test
    void getUser() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        assertTrue(userService.getUser("someId").isPresent());
    }

    @Test
    void getUserByUserName() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(user));
        assertTrue(userService.getUserByUserName("testuser").isPresent());
    }

    @Test
    void saveUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUserName());
    }

    @Test
    void updateUser() {
        userService.updateUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteUser() {
        userService.deleteUser("userId");
        verify(userRepository, times(1)).deleteById("userId");
    }

    @Test
    void sendOtpEmail_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.sendOtpEmail("unknownUser"));
        assertEquals(Constants.USER_NOT_EXISTS_CODE, thrown.getError().getErrorCode());
    }

    @Test
    void sendOtpEmail_Success() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        userService.sendOtpEmail("testuser");
        assertNotNull(user.getOtpCode());
    }

    @Test
    void validateOtp_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.validateOtp("unknownUser", "123456", "2021-01-01 00:00:00"));
        assertEquals(Constants.USER_NOT_EXISTS_CODE, thrown.getError().getErrorCode());
    }

    @Test
    void validateOtp_InvalidOtp() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(user));
        user.setOtpCode("654321");
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.validateOtp("testuser", "123456", "2021-01-01 00:00:00"));
        assertEquals(Constants.INVALID_OTP_CODE, thrown.getError().getErrorCode());
    }

    @Test
    void validateOtp_Success() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(user));
        user.setOtpCode("123456");
        userService.validateOtp("testuser", "123456", "2021-01-01 00:00:00");
        assertNull(user.getOtpCode());
    }

    @Test
    void saveUserOnAzureAd_UserAlreadyExists() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(user));
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.saveUserOnAzureAd(user));
        assertEquals(Constants.USER_ALREADY_EXISTS_CODE, thrown.getError().getErrorCode());
    }

    @Test
    void saveUserOnAzureAd_Success() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponse response = userService.saveUserOnAzureAd(user);
        assertEquals(HttpStatus.CREATED, response.getCode());
    }

    @Test
    void deleteUserOnAzureAd_UserNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.deleteUserOnAzureAd("userId"));
        assertEquals(Constants.USER_NOT_FOUND_CODE, thrown.getError().getErrorCode());
    }

    @Test
    void deleteUserOnAzureAd_Success() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getAzureId()).thenReturn("azureId");
        userService.deleteUserOnAzureAd("userId");
        verify(userRepository, times(1)).deleteById("userId");
    }
}