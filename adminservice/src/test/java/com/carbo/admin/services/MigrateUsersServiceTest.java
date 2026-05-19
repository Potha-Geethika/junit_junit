package com.carbo.admin.services;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.model.Error;
import com.carbo.admin.model.Organization;
import com.carbo.admin.model.Role;
import com.carbo.admin.model.User;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponseDTO;
import com.carbo.admin.repository.UserMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;






@ExtendWith(MockitoExtension.class)
class MigrateUsersServiceTest {

    @Mock
    private UserMongoDbRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MigrateUsersService migrateUsersService;

    @BeforeEach
    void setUp() {
        migrateUsersService = new MigrateUsersService(userRepository, mongoTemplate);
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved_HappyPath() {
        List<AiUser> aiUsers = List.of(new AiUser("1", 1, "tenant1", 1, "username1", "name1", "surname1", "email@example.com", "1234567890", "role", "notificationType", "active"));
        List<User> allUsers = Collections.emptyList();
        when(userRepository.findAll()).thenReturn(allUsers);

        Set<Organization> organizations = Set.of(new Organization());
        when(mongoTemplate.find(any(Query.class), any())).thenReturn(new ArrayList<>(organizations));

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved_OrganizationNotFound() {
        List<AiUser> aiUsers = List.of(new AiUser("1", 1, "tenant1", 1, "username1", "name1", "surname1", "email@example.com", "1234567890", "role", "notificationType", "active"));
        List<User> allUsers = Collections.emptyList();
        when(userRepository.findAll()).thenReturn(allUsers);
        when(mongoTemplate.find(any(Query.class), any())).thenReturn(Collections.emptyList());

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Organization is not found with the tenant name", result.get(0).getStatus());
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved_UsernameAlreadyExists() {
        List<AiUser> aiUsers = List.of(new AiUser("1", 1, "tenant1", 1, "username1", "name1", "surname1", "email@example.com", "1234567890", "role", "notificationType", "active"));
        User existingUser = new User();
        existingUser.setUserName("username1");
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Username already exists in db", result.get(0).getStatus());
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved_ExceptionHandling() {
        List<AiUser> aiUsers = List.of(new AiUser("1", 1, "tenant1", 1, "username1", "name1", "surname1", "email@example.com", "1234567890", "role", "notificationType", "active"));
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database Error"));

        ErrorException exception = org.junit.jupiter.api.Assertions.assertThrows(ErrorException.class, () -> {
            migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);
        });

        assertNotNull(exception);
        assertEquals(HttpStatus.BAD_REQUEST.toString(), exception.getError().getErrorCode());
        assertEquals("Some Error occurred : Database Error", exception.getError().getErrorMessage());
    }

    @Test
    void testGetAllUsersForAi() {
        List<UserResponseDTO> users = List.of(new UserResponseDTO());
        when(userRepository.findAllUsersWithSelectedFields()).thenReturn(users);
        when(mongoTemplate.findAll(Organization.class)).thenReturn(Collections.emptyList());

        List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();
        assertNotNull(result);
        assertEquals(users.size(), result.size());
    }

    @Test
    void testSetUserAzureId_HappyPath() {
        List<AiUser> aiUsers = List.of(new AiUser("1", 1, "tenant1", 1, "username1", "name1", "surname1", "email@example.com", "1234567890", "role", "notificationType", "active"));
        User existingUser = new User();
        existingUser.setUserName("username1");
        existingUser.setAzureId("oldAzureId");
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<User> result = migrateUsersService.setUserAzureId(aiUsers);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getAzureId());
    }

    @Test
    void testSetUserAzureId_ExceptionHandling() {
        List<AiUser> aiUsers = List.of(new AiUser("1", 1, "tenant1", 1, "username1", "name1", "surname1", "email@example.com", "1234567890", "role", "notificationType", "active"));
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database Error"));

        ErrorException exception = org.junit.jupiter.api.Assertions.assertThrows(ErrorException.class, () -> {
            migrateUsersService.setUserAzureId(aiUsers);
        });

        assertNotNull(exception);
        assertEquals(HttpStatus.BAD_REQUEST.toString(), exception.getError().getErrorCode());
        assertEquals("Some Error occurred : Database Error", exception.getError().getErrorMessage());
    }
}