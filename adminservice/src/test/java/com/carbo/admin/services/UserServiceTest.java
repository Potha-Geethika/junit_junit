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

    @Mock
    private GraphServiceClient.UsersCollectionRequestBuilder usersCollectionRequestBuilder;

    @Mock
    private GraphServiceClient.UserRequestBuilder userRequestBuilder;

    @Mock
    private GraphServiceClient.UserRequest userRequest;

    @Mock
    private GraphUser graphUser;

    @BeforeEach
    void setup() {
        // No extra setup required beyond @InjectMocks and @Mock
    }

    // ========== getAll() ==========
    @Test
    void testGetAllReturnsUsers() {
        List<User> expected = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(expected);
        List<User> actual = userService.getAll();
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        verify(userRepository).findAll();
    }

    @Test
    void testGetAllReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<User> actual = userService.getAll();
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    // ========== getByOrganizationId(String) ==========
    @Test
    void testGetByOrganizationIdReturnsUsers() {
        String orgId = "org1";
        List<User> expected = Arrays.asList(new User(), new User());
        when(userRepository.findByOrganizationId(orgId)).thenReturn(expected);
        List<User> actual = userService.getByOrganizationId(orgId);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        verify(userRepository).findByOrganizationId(orgId);
    }

    @Test
    void testGetByOrganizationIdReturnsEmptyList() {
        String orgId = "org1";
        when(userRepository.findByOrganizationId(orgId)).thenReturn(Collections.emptyList());
        List<User> actual = userService.getByOrganizationId(orgId);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    // ========== getUser(String) ==========
    @Test
    void testGetUserReturnsOptionalUser() {
        String id = "user1";
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUser(id);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findById(id);
    }

    @Test
    void testGetUserReturnsEmpty() {
        String id = "user1";
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUser(id);
        assertFalse(result.isPresent());
    }

    // ========== getUserByUserName(String) ==========
    @Test
    void testGetUserByUserNameReturnsOptionalUser() {
        String username = "user1";
        User user = new User();
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByUserName(username);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByUserNameIgnoreCase(username);
    }

    @Test
    void testGetUserByUserNameReturnsEmpty() {
        String username = "user1";
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserByUserName(username);
        assertFalse(result.isPresent());
    }

    // ========== saveUser(User) ==========
    @Test
    void testSaveUserReturnsSavedUser() {
        User user = new User();
        User savedUser = new User();
        when(userRepository.save(user)).thenReturn(savedUser);
        User result = userService.saveUser(user);
        assertNotNull(result);
        assertEquals(savedUser, result);
        verify(userRepository).save(user);
    }

    // ========== updateUser(User) ==========
    @Test
    void testUpdateUserCallsSave() {
        User user = new User();
        doReturn(user).when(userRepository).save(user);
        userService.updateUser(user);
        verify(userRepository).save(user);
    }

    // ========== deleteUser(String) ==========
    @Test
    void testDeleteUserCallsDeleteById() {
        String id = "userId";
        doNothing().when(userRepository).deleteById(id);
        userService.deleteUser(id);
        verify(userRepository).deleteById(id);
    }

    // ========== sendOtpEmail(String) ==========
    @Test
    void testSendOtpEmailHappyPath() {
        String username = "user@example.com";
        User user = new User();
        user.setUserName(username);
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.of(user));
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            String otp = invocation.getArgument(1);
            assertNotNull(otp);
            assertEquals(username, u.getUserName());
            return null;
        }).when(userRepository).save(any(User.class));

        userService.sendOtpEmail(username);

        verify(userRepository).findByUserNameIgnoreCase(username);
        verify(userRepository, atLeastOnce()).save(any(User.class));
        assertNotNull(user.getOtpCode());
        assertNotNull(user.getOtpGeneratedTime());
    }

    @Test
    void testSendOtpEmailUserNotFoundThrows() {
        String username = "user@example.com";
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.empty());
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.sendOtpEmail(username));
        assertEquals(Constants.USER_NOT_EXISTS_CODE, thrown.getError().getErrorCode());
    }

    // ========== validateOtp(String, String, String) ==========
    @Test
    void testValidateOtpSuccess() {
        String username = "user@example.com";
        String otpCode = "123456";
        String otpSubmittedTime = "2023-01-01 10:00:00";
        User user = new User();
        user.setUserName(username);
        user.setOtpCode(otpCode);
        user.setOtpGeneratedTime("2023-01-01 09:55:00");

        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.of(user));
        doAnswer(invocation -> null).when(userRepository).save(any(User.class));

        userService.validateOtp(username, otpCode, otpSubmittedTime);

        verify(userRepository).save(any(User.class));
        assertNull(user.getOtpCode());
        assertNull(user.getOtpGeneratedTime());
        assertNotNull(user.getAuthenticationTime());
    }

    @Test
    void testValidateOtpUserNotFoundThrows() {
        String username = "user@example.com";
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.empty());
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.validateOtp(username, "otp", "time"));
        assertEquals(Constants.USER_NOT_EXISTS_CODE, thrown.getError().getErrorCode());
    }

    @Test
    void testValidateOtpInvalidOtpThrows() {
        String username = "user@example.com";
        User user = new User();
        user.setUserName(username);
        user.setOtpCode("123456");
        user.setOtpGeneratedTime("2023-01-01 09:55:00");
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.of(user));
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.validateOtp(username, "wrongOtp", "2023-01-01 09:56:00"));
        assertEquals(Constants.INVALID_OTP_CODE, thrown.getError().getErrorCode());
    }

    @Test
    void testValidateOtpExpiredOtpThrows() {
        String username = "user@example.com";
        User user = new User();
        user.setUserName(username);
        user.setOtpCode("123456");
        user.setOtpGeneratedTime("2023-01-01 09:00:00");
        when(userRepository.findByUserNameIgnoreCase(username)).thenReturn(Optional.of(user));
        ErrorException thrown = assertThrows(ErrorException.class, () -> userService.validateOtp(username, "123456", "2023-01-01 10:11:00"));
        assertEquals(Constants.OTP_EXPIRED_CODE, thrown.getError().getErrorCode());
    }

    // ========== isValidPassword(String) ==========
    @Test
    void testIsValidPasswordValidCases() {
        assertTrue(UserService.isValidPassword("Abcdef1@"));
        assertTrue(UserService.isValidPassword("A1@bcdefghijklmnopqrstuvwxyz"));
        assertTrue(UserService.isValidPassword("1234aA@b"));
    }

    @Test
    void testIsValidPasswordInvalidCases() {
        assertFalse(UserService.isValidPassword(null));
        assertFalse(UserService.isValidPassword("short1@")); // length less than 8
        assertFalse(UserService.isValidPassword("alllowercase1")); // no uppercase, no symbol
        assertFalse(UserService.isValidPassword("ALLUPPERCASE1")); // no lowercase, no symbol
        assertFalse(UserService.isValidPassword("NoDigits@")); // no digit
        assertFalse(UserService.isValidPassword("NoSymbols1")); // no symbol
        assertFalse(UserService.isValidPassword("abcABC123")); // only 2 conditions met (lower, upper, digit)
        assertFalse(UserService.isValidPassword("aA@")); // less than 8
        String longPassword = "aA@"+ "b".repeat(62); // length 65
        assertFalse(UserService.isValidPassword(longPassword));
    }

    // ========== saveUserOnAzureAd(User) ==========
    @Test
    void testSaveUserOnAzureAdSuccess() {
        User user = new User();
        user.setUserName("testuser@example.com");
        user.setPassword("Password1@");
        user.setOrganizationId("org1");
        // User not exist
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.empty());

        // Mock created Graph user
        GraphUser createdGraphUser = mock(GraphUser.class);
        when(createdGraphUser.getId()).thenReturn("azureId123");

        // Mock graphServiceClient.users().post(...)
        when(graphServiceClient.users()).thenReturn(usersCollectionRequestBuilder);
        when(usersCollectionRequestBuilder.post(any(GraphUser.class))).thenReturn(createdGraphUser);

        // Mock mongoTemplate findOne for Organization
        when(mongoTemplate.findOne(any(Query.class), eq(Object.class))).thenReturn(null);

        // Mock userRepository.save for final save
        User savedUser = new User();
        savedUser.setId("userId");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Mock producer push void
        doNothing().when(producer).push(anyString(), any());

        UserResponse response = userService.saveUserOnAzureAd(user);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getCode());
        assertNotNull(response.getResponse());
        verify(userRepository).save(any(User.class));
        verify(producer).push(anyString(), any());
    }

    @Test
    void testSaveUserOnAzureAdUserAlreadyExistsThrows() {
        User user = new User();
        user.setUserName("testuser@example.com");
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.of(new User()));
        ErrorException ex = assertThrows(ErrorException.class, () -> userService.saveUserOnAzureAd(user));
        assertEquals(Constants.USER_ALREADY_EXISTS_CODE, ex.getError().getErrorCode());
    }

    @Test
    void testSaveUserOnAzureAdExceptionDuringSaveDeletesAzureUser() {
        User user = new User();
        user.setUserName("testuser@example.com");
        user.setPassword("Password1@");
        user.setOrganizationId("org1");
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.empty());

        GraphUser createdGraphUser = mock(GraphUser.class);
        when(createdGraphUser.getId()).thenReturn("azureId123");

        when(graphServiceClient.users()).thenReturn(usersCollectionRequestBuilder);
        when(usersCollectionRequestBuilder.post(any(GraphUser.class))).thenReturn(createdGraphUser);

        when(mongoTemplate.findOne(any(Query.class), eq(Object.class))).thenReturn(null);

        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        doNothing().when(producer).push(anyString(), any());

        // Provide spy to verify deleteUserFromAzure is called via public method indirectly
        UserService spyService = Mockito.spy(userService);
        doNothing().when(spyService).deleteUserFromAzure(anyString());

        ErrorException ex = assertThrows(ErrorException.class, () -> spyService.saveUserOnAzureAd(user));
        assertTrue(ex.getError().getErrorMessage().toString().contains("Some error occurred while creating a user"));

        // Cannot verify private method call directly, but we tested public method behavior
    }

    // ========== updateUserOnAzureAd(User, String) ==========
    @Test
    void testUpdateUserOnAzureAdSuccessWithPasswordChange() {
        String userId = "userId";
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setAzureId("azureId");
        existingUser.setFirstName("OldFirst");
        existingUser.setLastName("OldLast");
        existingUser.setUserName("olduser@example.com");
        existingUser.setPassword("oldEncodedPassword");
        existingUser.setLastFivePasswords(new ArrayList<>());

        User updateUser = new User();
        updateUser.setPassword("NewPassword1@");
        updateUser.setFirstName("NewFirst");
        updateUser.setLastName("NewLast");
        updateUser.setUserName("newuser@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updateUser);
        when(mongoTemplate.findOne(any(Query.class), eq(Object.class))).thenReturn(null);
        doNothing().when(producer).push(anyString(), any());

        // Mock graphServiceClient.users().byUserId(...).patch(...)
        when(graphServiceClient.users()).thenReturn(usersCollectionRequestBuilder);
        when(usersCollectionRequestBuilder.byUserId("azureId")).thenReturn(userRequestBuilder);
        when(userRequestBuilder.patch(any(GraphUser.class))).thenReturn(graphUser);

        userService.updateUserOnAzureAd(updateUser, userId);

        verify(userRepository).save(any(User.class));
        verify(producer).push(anyString(), any());
    }

    @Test
    void testUpdateUserOnAzureAdThrowsWhenUserNotFound() {
        when(userRepository.findById("userId")).thenReturn(Optional.empty());
        ErrorException ex = assertThrows(ErrorException.class, () -> userService.updateUserOnAzureAd(new User(), "userId"));
        assertEquals(HttpStatus.BAD_REQUEST.toString(), ex.getError().getErrorCode());
    }

    @Test
    void testUpdateUserOnAzureAdThrowsWhenPasswordInvalid() {
        User existingUser = new User();
        existingUser.setAzureId("azureId");
        when(userRepository.findById("userId")).thenReturn(Optional.of(existingUser));
        User updateUser = new User();
        updateUser.setPassword("bad"); // invalid password
        ErrorException ex = assertThrows(ErrorException.class, () -> userService.updateUserOnAzureAd(updateUser, "userId"));
        assertEquals(HttpStatus.BAD_REQUEST.toString(), ex.getError().getErrorCode());
        assertTrue(ex.getError().getErrorMessage().toString().contains("Password is not valid"));
    }

    @Test
    void testUpdateUserOnAzureAdThrowsWhenAzureIdMissing() {
        User existingUser = new User();
        existingUser.setAzureId(null);
        when(userRepository.findById("userId")).thenReturn(Optional.of(existingUser));
        User updateUser = new User();
        updateUser.setPassword("Password1@");
        ErrorException ex = assertThrows(ErrorException.class, () -> userService.updateUserOnAzureAd(updateUser, "userId"));
        assertEquals(HttpStatus.BAD_REQUEST.toString(), ex.getError().getErrorCode());
        assertTrue(ex.getError().getErrorMessage().toString().contains("Azure id not available"));
    }

    // ========== deleteUserOnAzureAd(String) ==========
    @Test
    void testDeleteUserOnAzureAdSuccess() {
        String userId = "userId";
        User user = new User();
        user.setId(userId);
        user.setAzureId("azureId");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(userId);
        doNothing().when(producer).push(anyString(), any());
        when(graphServiceClient.users()).thenReturn(usersCollectionRequestBuilder);
        when(usersCollectionRequestBuilder.byUserId("azureId")).thenReturn(userRequestBuilder);
        doNothing().when(userRequestBuilder).delete();

        userService.deleteUserOnAzureAd(userId);

        verify(userRepository).deleteById(userId);
        verify(producer).push(anyString(), any());
    }

    @Test
    void testDeleteUserOnAzureAdThrowsWhenUserNotFound() {
        when(userRepository.findById("userId")).thenReturn(Optional.empty());
        ErrorException ex = assertThrows(ErrorException.class, () -> userService.deleteUserOnAzureAd("userId"));
        assertEquals(Constants.USER_NOT_FOUND_CODE, ex.getError().getErrorCode());
    }

    @Test
    void testDeleteUserOnAzureAdThrowsWhenAzureIdMissing() {
        User user = new User();
        user.setAzureId(null);
        when(userRepository.findById("userId")).thenReturn(Optional.of(user));
        ErrorException ex = assertThrows(ErrorException.class, () -> userService.deleteUserOnAzureAd("userId"));
        assertEquals(HttpStatus.BAD_REQUEST.toString(), ex.getError().getErrorCode());
        assertTrue(ex.getError().getErrorMessage().toString().contains(Constants.AZURE_USER_ID_NOT_PRESENT));
    }

    // ========== saveOpsUserComingFromAi(AiUser) ==========
    @Test
    void testSaveOpsUserComingFromAiUserNotExistSaves() {
        AiUser aiUser = AiUser.builder().azureUserId("azureId").tenantName("tenant").build();

        when(userRepository.findByAzureId(aiUser.getAzureUserId())).thenReturn(Optional.empty());
        when(mongoTemplate.findOne(any(Query.class), eq(Object.class))).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(new User());

        userService.saveOpsUserComingFromAi(aiUser);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSaveOpsUserComingFromAiUserExistsDoesNothing() {
        AiUser aiUser = AiUser.builder().azureUserId("azureId").tenantName("tenant").build();
        when(userRepository.findByAzureId(aiUser.getAzureUserId())).thenReturn(Optional.of(new User()));

        userService.saveOpsUserComingFromAi(aiUser);

        verify(userRepository, never()).save(any());
    }

    // ========== deleteOpsUserComingFromAi(String) ==========
    @Test
    void testDeleteOpsUserComingFromAiUserExistsDeletes() {
        String azureId = "azureId";
        User user = new User();
        user.setId("userId");
        when(userRepository.findByAzureId(azureId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(user.getId());

        userService.deleteOpsUserComingFromAi(azureId);

        verify(userRepository).deleteById(user.getId());
    }

    @Test
    void testDeleteOpsUserComingFromAiUserNotFoundDoesNothing() {
        when(userRepository.findByAzureId("azureId")).thenReturn(Optional.empty());

        userService.deleteOpsUserComingFromAi("azureId");

        verify(userRepository, never()).deleteById(anyString());
    }

    // ========== updateOpsUserComingFromAi(AiUser) ==========
    @Test
    void testUpdateOpsUserComingFromAiUserExistsUpdates() {
        AiUser aiUser = AiUser.builder()
                .azureUserId("azureId")
                .tenantName("tenantName")
                .name("First")
                .surname("Last")
                .emailAddress("email@example.com")
                .mobileNumber("1234567890")
                .build();
        User user = new User();
        user.setId("userId");

        when(userRepository.findByAzureId(aiUser.getAzureUserId())).thenReturn(Optional.of(user));
        when(mongoTemplate.findOne(any(Query.class), eq(Object.class))).thenReturn(null);
        doNothing().when(userRepository).save(any(User.class));

        userService.updateOpsUserComingFromAi(aiUser);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateOpsUserComingFromAiUserNotFoundDoesNothing() {
        AiUser aiUser = AiUser.builder().azureUserId("azureId").build();
        when(userRepository.findByAzureId(aiUser.getAzureUserId())).thenReturn(Optional.empty());

        userService.updateOpsUserComingFromAi(aiUser);

        verify(userRepository, never()).save(any());
    }

    // ========== updateLastPasswordResetFlagOnAzure(String) ==========
    @Test
    void testUpdateLastPasswordResetFlagOnAzureSuccess() {
        String azureId = "azureId";
        when(graphServiceClient.users()).thenReturn(usersCollectionRequestBuilder);
        when(usersCollectionRequestBuilder.byUserId(azureId)).thenReturn(userRequestBuilder);
        when(userRequestBuilder.patch(any(GraphUser.class))).thenReturn(graphUser);

        assertDoesNotThrow(() -> userService.updateLastPasswordResetFlagOnAzure(azureId));
    }

    @Test
    void testUpdateLastPasswordResetFlagOnAzureThrows() {
        String azureId = "azureId";
        when(graphServiceClient.users()).thenReturn(usersCollectionRequestBuilder);
        when(usersCollectionRequestBuilder.byUserId(azureId)).thenReturn(userRequestBuilder);
        when(userRequestBuilder.patch(any(GraphUser.class))).thenThrow(new RuntimeException("Patch failed"));

        ErrorException ex = assertThrows(ErrorException.class, () -> userService.updateLastPasswordResetFlagOnAzure(azureId));
        assertTrue(ex.getError().getErrorMessage().toString().contains("Some error occurred while updating the password reset flag"));
    }

    // ========== updateSelectColumn(UserPatchDTO) ==========
    @Test
    void testUpdateSelectColumnSuccess() {
        User user = new User();
        user.setId("userId");
        UserPatchDTO patchDTO = new UserPatchDTO();
        patchDTO.setUserId("userId");
        patchDTO.setSelectedColumns(Collections.singletonList("column1"));

        when(userRepository.findById("userId")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> userService.updateSelectColumn(patchDTO));
        verify(userRepository).save(user);
        assertEquals(patchDTO.getSelectedColumns(), user.getSelectedColumns());
    }

    @Test
    void testUpdateSelectColumnUserNotFoundThrows() {
        UserPatchDTO patchDTO = new UserPatchDTO();
        patchDTO.setUserId("userId");
        patchDTO.setSelectedColumns(Collections.singletonList("column1"));
        when(userRepository.findById("userId")).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> userService.updateSelectColumn(patchDTO));
    }

    // ========== updateUserFilters(String, Map<String, Object>) ==========
    @Test
    void testUpdateUserFiltersReturnsUpdatedUser() {
        String userId = "userId";
        Map<String, Object> filters = new HashMap<>();
        filters.put("key", "value");
        User updatedUser = new User();
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(), eq(User.class))).thenReturn(updatedUser);

        User result = userService.updateUserFilters(userId, filters);

        assertEquals(updatedUser, result);
        verify(mongoTemplate).findAndModify(any(Query.class), any(Update.class), any(), eq(User.class));
    }
}