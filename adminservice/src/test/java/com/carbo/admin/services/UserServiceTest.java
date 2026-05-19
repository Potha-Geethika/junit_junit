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
    private Producer producer;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<User> users = userService.getAll();
        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    void getByOrganizationId() {
        String organizationId = "org-123";
        when(userRepository.findByOrganizationId(organizationId)).thenReturn(Collections.emptyList());
        List<User> users = userService.getByOrganizationId(organizationId);
        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    void getUser() {
        String userId = "user-123";
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUser(userId);
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.get().getId());
    }

    @Test
    void getUserByUserName() {
        String userName = "testUser";
        User user = new User();
        user.setUserName(userName);
        when(userRepository.findByUserNameIgnoreCase(userName)).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUserByUserName(userName);
        assertNotNull(foundUser);
        assertEquals(userName, foundUser.get().getUserName());
    }

    @Test
    void saveUser() {
        User user = new User();
        user.setUserName("newUser");
        when(userRepository.save(user)).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals("newUser", savedUser.getUserName());
    }

    @Test
    void updateUser() {
        User user = new User();
        user.setId("user-123");
        when(userRepository.save(user)).thenReturn(user);
        userService.updateUser(user);
    }

    @Test
    void deleteUser() {
        String userId = "user-123";
        doNothing().when(userRepository).deleteById(userId);
        userService.deleteUser(userId);
    }

    @Test
    void sendOtpEmail_UserDoesNotExist() {
        String username = "nonExistentUser";
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.sendOtpEmail(username));
        assertEquals(Constants.USER_NOT_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void sendOtpEmail_Success() {
        String username = "existingUser";
        User user = new User();
        user.setUserName(username);
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).save(any(User.class));
        userService.sendOtpEmail(username);
    }

    @Test
    void validateOtp_UserDoesNotExist() {
        String username = "nonExistentUser";
        String submittedOtp = "123456";
        String otpSubmittedTime = "2023-01-01 10:00:00";
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.validateOtp(username, submittedOtp, otpSubmittedTime));
        assertEquals(Constants.USER_NOT_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void validateOtp_InvalidOtp() {
        String username = "existingUser";
        String submittedOtp = "wrongOtp";
        String otpSubmittedTime = "2023-01-01 10:00:00";
        User user = new User();
        user.setOtpCode("123456");
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.of(user));
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.validateOtp(username, submittedOtp, otpSubmittedTime));
        assertEquals(Constants.INVALID_OTP_CODE, exception.getError().getErrorCode());
    }

    @Test
    void validateOtp_Success() {
        String username = "existingUser";
        String submittedOtp = "123456";
        String otpSubmittedTime = "2023-01-01 10:00:00";
        User user = new User();
        user.setOtpCode(submittedOtp);
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).save(any(User.class));
        userService.validateOtp(username, submittedOtp, otpSubmittedTime);
    }

    @Test
    void saveUserOnAzureAd_UserAlreadyExists() {
        User user = new User();
        user.setUserName("existingUser");
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.of(user));
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.saveUserOnAzureAd(user));
        assertEquals(Constants.USER_ALREADY_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void saveUserOnAzureAd_Success() {
        User user = new User();
        user.setUserName("newUser");
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponse response = userService.saveUserOnAzureAd(user);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getCode());
    }

    @Test
    void updateUserOnAzureAd_UserNotFound() {
        User user = new User();
        String userId = "user-123";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.updateUserOnAzureAd(user, userId));
        assertEquals(Constants.USER_NOT_FOUND_CODE, exception.getError().getErrorCode());
    }

    @Test
    void updateUserOnAzureAd_Success() {
        User existingUser = new User();
        existingUser.setAzureId("azure-id");
        existingUser.setUserName("existingUser");
        String userId = "user-123";
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).save(any(User.class));
        userService.updateUserOnAzureAd(existingUser, userId);
    }

    @Test
    void deleteUserOnAzureAd_UserNotFound() {
        String userId = "user-123";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.deleteUserOnAzureAd(userId));
        assertEquals(Constants.USER_NOT_FOUND_CODE, exception.getError().getErrorCode());
    }

    @Test
    void deleteUserOnAzureAd_Success() {
        String userId = "user-123";
        User user = new User();
        user.setAzureId("azure-id");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(userId);
        userService.deleteUserOnAzureAd(userId);
    }

    @Test
    void updateOpsUserComingFromAi_UserNotFound() {
        AiUser aiUser = new AiUser();
        aiUser.setAzureUserId("azure-id");
        when(userRepository.findByAzureId(aiUser.getAzureUserId())).thenReturn(Optional.empty());
        userService.updateOpsUserComingFromAi(aiUser);
    }

    @Test
    void updateOpsUserComingFromAi_Success() {
        AiUser aiUser = new AiUser();
        aiUser.setAzureUserId("azure-id");
        User user = new User();
        user.setId("user-123");
        when(userRepository.findByAzureId(aiUser.getAzureUserId())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).save(any(User.class));
        userService.updateOpsUserComingFromAi(aiUser);
    }

    @Test
    void deleteOpsUserComingFromAi_UserNotFound() {
        String azureId = "azure-id";
        when(userRepository.findByAzureId(azureId)).thenReturn(Optional.empty());
        userService.deleteOpsUserComingFromAi(azureId);
    }

    @Test
    void deleteOpsUserComingFromAi_Success() {
        String azureId = "azure-id";
        User user = new User();
        user.setId("user-123");
        when(userRepository.findByAzureId(azureId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(user.getId());
        userService.deleteOpsUserComingFromAi(azureId);
    }

    @Test
    void updateLastPasswordResetFlagOnAzure_Success() {
        String azureId = "azure-id";
        doNothing().when(producer).push(anyString(), any());
        userService.updateLastPasswordResetFlagOnAzure(azureId);
    }
}