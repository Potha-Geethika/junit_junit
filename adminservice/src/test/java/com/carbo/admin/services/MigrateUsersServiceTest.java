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






class MigrateUsersServiceTest {

    @Mock
    private UserMongoDbRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MigrateUsersService migrateUsersService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveAiUsersAndCollectUnsaved_HappyPath() {
        List<AiUser> aiUsers = createAiUsers();
        List<User> existingUsers = new ArrayList<>();
        when(userRepository.findAll()).thenReturn(existingUsers);
        
        Organization organization = new Organization();
        organization.setId("org-1");
        organization.setName("Tenant1");
        when(mongoTemplate.find(any(), eq(Organization.class))).thenReturn(List.of(organization));

        List<User> savedUsers = new ArrayList<>();
        when(userRepository.saveAll(any())).thenAnswer(invocation -> {
            savedUsers.addAll(invocation.getArgument(0));
            return savedUsers;
        });

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(0, result.size());
        assertEquals(1, savedUsers.size());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_UsernameAlreadyExists() {
        List<AiUser> aiUsers = createAiUsers();
        User existingUser = new User();
        existingUser.setUserName("existing@example.com");
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Username already exists in db", result.get(0).getStatus());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_OrganizationNotFound() {
        List<AiUser> aiUsers = createAiUsers();
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(mongoTemplate.find(any(), eq(Organization.class))).thenReturn(Collections.emptyList());

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Organization is not found with the tenant name", result.get(0).getStatus());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_ErrorHandling() {
        List<AiUser> aiUsers = createAiUsers();
        when(userRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        try {
            migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);
        } catch (ErrorException e) {
            assertEquals("Some Error occurred : DB error", e.getError().getErrorMessage());
        }
    }

    @Test
    void getAllUsersForAi_HappyPath() {
        when(userRepository.findAllUsersWithSelectedFields()).thenReturn(Collections.emptyList());
        when(mongoTemplate.findAll(Organization.class)).thenReturn(Collections.emptyList());

        List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void setUserAzureId_HappyPath() {
        List<AiUser> aiUsers = createAiUsers();
        User existingUser = new User();
        existingUser.setUserName("user@example.com");
        existingUser.setAzureId(null);
        
        when(userRepository.findAll()).thenReturn(List.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        List<User> result = migrateUsersService.setUserAzureId(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("azure-id", result.get(0).getAzureId());
    }

    @Test
    void setUserAzureId_ErrorHandling() {
        List<AiUser> aiUsers = createAiUsers();
        when(userRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        try {
            migrateUsersService.setUserAzureId(aiUsers);
        } catch (ErrorException e) {
            assertEquals("Some Error occurred : DB error", e.getError().getErrorMessage());
        }
    }

    private List<AiUser> createAiUsers() {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("user@example.com");
        aiUser.setTenantName("Tenant1");
        aiUser.setAzureUserId("azure-id");
        return List.of(aiUser);
    }
}