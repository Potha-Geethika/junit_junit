package com.carbo.admin.controllers;

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

    private List<AiUser> aiUserList;
    private List<UserResponseDTO> userResponseDTOList;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        aiUserList = new ArrayList<>();
        userResponseDTOList = new ArrayList<>();
        userList = new ArrayList<>();
    }

    @Test
    void saveAiUsersAndCollectUnsaved_HappyPath() throws Exception {
        when(migrateusersService.saveAiUsersAndCollectUnsaved(anyList())).thenReturn(aiUserList);

        mockMvc.perform(post("/v1/user/migrate/saveAiUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"emailAddress\":\"test@example.com\",\"tenantName\":\"TestTenant\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(migrateusersService, times(1)).saveAiUsersAndCollectUnsaved(anyList());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_InternalServerError() throws Exception {
        when(migrateusersService.saveAiUsersAndCollectUnsaved(anyList())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(post("/v1/user/migrate/saveAiUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"emailAddress\":\"test@example.com\",\"tenantName\":\"TestTenant\"}]"))
                .andExpect(status().isInternalServerError());

        verify(migrateusersService, times(1)).saveAiUsersAndCollectUnsaved(anyList());
    }

    @Test
    void getAllUsersForAi_HappyPath() throws Exception {
        when(migrateusersService.getAllUsersForAi()).thenReturn(userResponseDTOList);

        mockMvc.perform(get("/v1/user/migrate/getAllUsersForAi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(migrateusersService, times(1)).getAllUsersForAi();
    }

    @Test
    void setUserAzureId_HappyPath() throws Exception {
        when(migrateusersService.setUserAzureId(anyList())).thenReturn(userList);

        mockMvc.perform(put("/v1/user/migrate/setUserAzureId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"emailAddress\":\"test@example.com\",\"azureUserId\":\"12345\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(migrateusersService, times(1)).setUserAzureId(anyList());
    }

    @Test
    void setUserAzureId_InternalServerError() throws Exception {
        when(migrateusersService.setUserAzureId(anyList())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(put("/v1/user/migrate/setUserAzureId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"emailAddress\":\"test@example.com\",\"azureUserId\":\"12345\"}]"))
                .andExpect(status().isInternalServerError());

        verify(migrateusersService, times(1)).setUserAzureId(anyList());
    }
}