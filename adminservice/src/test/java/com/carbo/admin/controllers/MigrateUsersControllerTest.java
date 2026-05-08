package com.carbo.admin.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.admin.model.User;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponseDTO;
import com.carbo.admin.services.MigrateUsersService;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;







@WebMvcTest(MigrateUsersController.class)
class MigrateUsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MigrateUsersService migrateusersService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void saveAiUsersAndCollectUnsaved_HappyPath() throws Exception {
        List<AiUser> unsavedUsers = new ArrayList<>();
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("existing@example.com");
        aiUser.setStatus("User not created : Username already exists in db");
        unsavedUsers.add(aiUser);

        Mockito.when(migrateusersService.saveAiUsersAndCollectUnsaved(any())).thenReturn(unsavedUsers);

        String jsonRequest = objectMapper.writeValueAsString(List.of(new AiUser()));

        mockMvc.perform(post("/v1/user/migrate/saveAiUsers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].emailAddress", is("existing@example.com")))
                .andExpect(jsonPath("$[0].status", is("User not created : Username already exists in db")));

        verify(migrateusersService).saveAiUsersAndCollectUnsaved(any());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_InternalServerError() throws Exception {
        doThrow(new RuntimeException("Unexpected error"))
                .when(migrateusersService).saveAiUsersAndCollectUnsaved(any());

        String jsonRequest = objectMapper.writeValueAsString(List.of(new AiUser()));

        mockMvc.perform(post("/v1/user/migrate/saveAiUsers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isInternalServerError());

        verify(migrateusersService).saveAiUsersAndCollectUnsaved(any());
    }

    @Test
    void getAllUsersForAi_HappyPath() throws Exception {
        List<UserResponseDTO> users = new ArrayList<>();
        UserResponseDTO userDto = new UserResponseDTO();
        userDto.setUserName("testuser");
        userDto.setOrganizationId("org1");
        userDto.setOrganizationName("OrgName");
        users.add(userDto);

        Mockito.when(migrateusersService.getAllUsersForAi()).thenReturn(users);

        mockMvc.perform(get("/v1/user/migrate/getAllUsersForAi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userName", is("testuser")))
                .andExpect(jsonPath("$[0].organizationId", is("org1")))
                .andExpect(jsonPath("$[0].organizationName", is("OrgName")));

        verify(migrateusersService).getAllUsersForAi();
    }

    @Test
    void getAllUsersForAi_InternalServerError() throws Exception {
        doThrow(new RuntimeException("Unexpected error"))
                .when(migrateusersService).getAllUsersForAi();

        mockMvc.perform(get("/v1/user/migrate/getAllUsersForAi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(migrateusersService).getAllUsersForAi();
    }

    @Test
    void setUserAzureId_HappyPath() throws Exception {
        List<User> updatedUsers = new ArrayList<>();
        User user = new User();
        user.setUserName("testuser");
        user.setAzureId("azure123");
        updatedUsers.add(user);

        Mockito.when(migrateusersService.setUserAzureId(any())).thenReturn(updatedUsers);

        String jsonRequest = objectMapper.writeValueAsString(List.of(new AiUser()));

        mockMvc.perform(put("/v1/user/migrate/setUserAzureId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userName", is("testuser")))
                .andExpect(jsonPath("$[0].azureId", is("azure123")));

        verify(migrateusersService).setUserAzureId(any());
    }

    @Test
    void setUserAzureId_InternalServerError() throws Exception {
        doThrow(new RuntimeException("Unexpected error"))
                .when(migrateusersService).setUserAzureId(any());

        String jsonRequest = objectMapper.writeValueAsString(List.of(new AiUser()));

        mockMvc.perform(put("/v1/user/migrate/setUserAzureId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isInternalServerError());

        verify(migrateusersService).setUserAzureId(any());
    }
}