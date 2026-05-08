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

  @Mock private UserMongoDbRepository userRepository;

  @Mock private MongoTemplate mongoTemplate;

  @InjectMocks private MigrateUsersService migrateUsersService;

  private AiUser aiUser1;
  private AiUser aiUser2;
  private User existingUser;
  private Organization organization1;
  private Organization organization2;

  @BeforeEach
  void setUp() {
    aiUser1 = new AiUser();
    aiUser1.setTenantName("Tenant A");
    aiUser1.setEmailAddress("email1@example.com");

    aiUser2 = new AiUser();
    aiUser2.setTenantName("Tenant B");
    aiUser2.setEmailAddress("existingUserName");

    existingUser = new User();
    existingUser.setUserName("existingUserName");

    organization1 = new Organization();
    organization1.setId("org1");
    organization1.setName("Tenant A");

    organization2 = new Organization();
    organization2.setId("org2");
    organization2.setName("Tenant B");
  }

  @Test
  void saveAiUsersAndCollectUnsaved_happyPath() {
    List<AiUser> aiUsers = new ArrayList<>();
    aiUser1.setEmailAddress("newuser@example.com");
    aiUser1.setTenantName("Tenant A");
    aiUser2.setEmailAddress("existinguser@example.com");
    aiUser2.setTenantName("Tenant B");
    aiUsers.add(aiUser1);
    aiUsers.add(aiUser2);

    User userFromDb = new User();
    userFromDb.setUserName("existinguser@example.com");

    List<User> allUsers = List.of(userFromDb);

    when(userRepository.findAll()).thenReturn(allUsers);
    when(mongoTemplate.find(any(Query.class), any())).thenReturn(List.of(organization1, organization2));
    doReturn(Collections.emptyList()).when(userRepository).saveAll(any());

    List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

    assertNotNull(result);
    // Both users have unique emails, so no user should be returned as existing
    // But aiUser2's email matches existingUser so expect it to be marked as existing
    boolean foundExistingStatus = result.stream().anyMatch(u -> u.getStatus() != null);
    assertEquals(true, foundExistingStatus);
  }

  @Test
  void saveAiUsersAndCollectUnsaved_existingUserName_caseInsensitive() {
    List<AiUser> aiUsers = new ArrayList<>();
    AiUser user = new AiUser();
    user.setEmailAddress("EXISTINGUSERNAME");
    user.setTenantName("Tenant A");
    aiUsers.add(user);

    User userFromDb = new User();
    userFromDb.setUserName("existingusername");

    List<User> allUsers = List.of(userFromDb);

    when(userRepository.findAll()).thenReturn(allUsers);
    when(mongoTemplate.find(any(Query.class), any())).thenReturn(List.of(organization1));

    List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("User not created : Username already exists in db", result.get(0).getStatus());
  }

  @Test
  void saveAiUsersAndCollectUnsaved_organizationListEmpty() {
    List<AiUser> aiUsers = new ArrayList<>();
    aiUsers.add(aiUser1);

    when(userRepository.findAll()).thenReturn(Collections.emptyList());
    when(mongoTemplate.find(any(Query.class), any())).thenReturn(Collections.emptyList());

    List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("User not created : Organization is not found with the tenant name", result.get(0).getStatus());
  }

  @Test
  void saveAiUsersAndCollectUnsaved_throwsException() {
    when(userRepository.findAll()).thenThrow(new RuntimeException("DB error"));

    ErrorException thrown =
        assertThrows(
            ErrorException.class,
            () -> migrateUsersService.saveAiUsersAndCollectUnsaved(List.of(aiUser1)));

    assertNotNull(thrown.getError());
    assertEquals("400 BAD_REQUEST", thrown.getError().getErrorCode());
  }

  @Test
  void convertAiUserToOpsUser_happyPath() throws Exception {
    AiUser input = new AiUser();
    input.setEmailAddress("user@example.com");
    input.setName("First");
    input.setSurname("Last");
    input.setAzureUserId("azure-123");

    // Using reflection to access private method - not allowed, so test indirectly via saveAiUsersAndCollectUnsaved
    // Instead, test via public method:
    List<AiUser> aiUsers = new ArrayList<>();
    aiUsers.add(input);

    when(userRepository.findAll()).thenReturn(Collections.emptyList());
    when(mongoTemplate.find(any(Query.class), any())).thenReturn(List.of(organization1));
    doReturn(Collections.emptyList()).when(userRepository).saveAll(any());

    List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

    assertNotNull(result);
    assertEquals(0, result.size()); // No existing users returned
  }

  @Test
  void convertAiUserToOpsUser_throwsException() {
    AiUser input = new AiUser() {
      @Override
      public String getEmailAddress() {
        throw new RuntimeException("Fail");
      }
    };
    List<AiUser> aiUsers = List.of(input);

    when(userRepository.findAll()).thenReturn(Collections.emptyList());
    when(mongoTemplate.find(any(Query.class), any())).thenReturn(List.of(organization1));

    ErrorException thrown =
        assertThrows(
            ErrorException.class,
            () -> migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers));

    assertNotNull(thrown.getError());
    assertEquals("400 BAD_REQUEST", thrown.getError().getErrorCode());
  }

  @Test
  void getAllUsersForAi_happyPath() {
    UserResponseDTO u1 = new UserResponseDTO();
    u1.setOrganizationId("org1");
    UserResponseDTO u2 = new UserResponseDTO();
    u2.setOrganizationId("org2");
    List<UserResponseDTO> users = List.of(u1, u2);

    Organization o1 = new Organization();
    o1.setId("org1");
    o1.setName("Organization1");
    Organization o2 = new Organization();
    o2.setId("org2");
    o2.setName("Organization2");
    List<Organization> orgs = List.of(o1, o2);

    doReturn(users).when(userRepository).findAllUsersWithSelectedFields();
    doReturn(orgs).when(mongoTemplate).findAll(Organization.class);

    List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Organization1", result.get(0).getOrganizationName());
    assertEquals("Organization2", result.get(1).getOrganizationName());
  }

  @Test
  void getAllUsersForAi_noOrganizations() {
    UserResponseDTO u1 = new UserResponseDTO();
    u1.setOrganizationId("org1");
    List<UserResponseDTO> users = List.of(u1);

    doReturn(users).when(userRepository).findAllUsersWithSelectedFields();
    doReturn(Collections.emptyList()).when(mongoTemplate).findAll(Organization.class);

    List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(null, result.get(0).getOrganizationName());
  }

  @Test
  void setUserAzureId_happyPath() {
    AiUser aiUser = new AiUser();
    aiUser.setEmailAddress("user@example.com");
    aiUser.setAzureUserId("azure-123");

    User user = new User();
    user.setUserName("user@example.com");

    when(userRepository.findAll()).thenReturn(List.of(user));
    doReturn(user).when(userRepository).save(user);

    List<User> updatedUsers = migrateUsersService.setUserAzureId(List.of(aiUser));

    assertNotNull(updatedUsers);
    assertEquals(1, updatedUsers.size());
    assertEquals("azure-123", updatedUsers.get(0).getAzureId());
  }

  @Test
  void setUserAzureId_noMatch() {
    AiUser aiUser = new AiUser();
    aiUser.setEmailAddress("notfound@example.com");
    aiUser.setAzureUserId("azure-123");

    User user = new User();
    user.setUserName("user@example.com");

    when(userRepository.findAll()).thenReturn(List.of(user));

    List<User> updatedUsers = migrateUsersService.setUserAzureId(List.of(aiUser));

    assertNotNull(updatedUsers);
    assertEquals(0, updatedUsers.size());
  }

  @Test
  void setUserAzureId_throwsException() {
    when(userRepository.findAll()).thenThrow(new RuntimeException("DB failure"));

    ErrorException thrown =
        assertThrows(
            ErrorException.class,
            () -> migrateUsersService.setUserAzureId(List.of(aiUser1)));

    assertNotNull(thrown.getError());
    assertEquals("400 BAD_REQUEST", thrown.getError().getErrorCode());
  }
}