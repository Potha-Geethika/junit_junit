package com.carbo.checklist.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.checklist.model.CheckList;
import com.carbo.checklist.services.CheckListService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.*;
import static com.carbo.checklist.utils.ControllerUtil.getOrganizationId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;






@WebMvcTest(CheckListServiceController.class)
class CheckListServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckListService checkListService;

    private static final String BASE_URL = "/v1/checklists";

    // Utility method to create JSON of CheckList for request bodies
    private String toJson(CheckList checkList) {
        // Minimal JSON representation for required fields only (jobId, day, shift, organizationId)
        // ignoring items and other optional fields for simplicity
        StringBuilder sb = new StringBuilder("{");
        if (checkList.getJobId() != null) {
            sb.append("\"jobId\":\"").append(checkList.getJobId()).append("\",");
        }
        if (checkList.getDay() != null) {
            sb.append("\"day\":").append(checkList.getDay()).append(",");
        }
        if (checkList.getShift() != null) {
            sb.append("\"shift\":\"").append(checkList.getShift()).append("\",");
        }
        if (checkList.getOrganizationId() != null) {
            sb.append("\"organizationId\":\"").append(checkList.getOrganizationId()).append("\",");
        }
        // remove trailing comma if exists
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    @Test
    @DisplayName("GET /v1/checklists/?jobId= - success 200 and JSON array")
    void getCheckLists_HappyPath() throws Exception {
        CheckList cl1 = new CheckList();
        cl1.setId("id1");
        cl1.setJobId("job1");
        CheckList cl2 = new CheckList();
        cl2.setId("id2");
        cl2.setJobId("job1");

        Mockito.when(checkListService.getByJobId(any())).thenReturn(List.of(cl1, cl2));

        mockMvc.perform(get(BASE_URL + "/").param("jobId", "job1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("id1"))
                .andExpect(jsonPath("$[0].jobId").value("job1"))
                .andExpect(jsonPath("$[1].id").value("id2"))
                .andExpect(jsonPath("$[1].jobId").value("job1"));

        verify(checkListService, times(1)).getByJobId("job1");
    }

    @Test
    @DisplayName("GET /v1/checklists/?jobId= - failure 500 due to IllegalArgumentException")
    void getCheckLists_MissingJobId_BadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/"))
                .andExpect(status().isInternalServerError()); // IllegalArgumentException is not handled, leads to 500

        // Service is never called because exception thrown before
        verify(checkListService, times(0)).getByJobId(any());
    }

    @Test
    @DisplayName("GET /v1/checklists/{checkListId} - success 200 with JSON")
    void getCheckList_HappyPath() throws Exception {
        CheckList cl = new CheckList();
        cl.setId("id1");
        cl.setJobId("job1");
        Mockito.when(checkListService.getCheckList(any())).thenReturn(Optional.of(cl));

        mockMvc.perform(get(BASE_URL + "/id1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("id1"))
                .andExpect(jsonPath("$.jobId").value("job1"));

        verify(checkListService, times(1)).getCheckList("id1");
    }

    @Test
    @DisplayName("GET /v1/checklists/{checkListId} - failure 500 when Optional empty")
    void getCheckList_EmptyOptional_InternalServerError() throws Exception {
        Mockito.when(checkListService.getCheckList(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/id1"))
                .andExpect(status().isInternalServerError());

        verify(checkListService, times(1)).getCheckList("id1");
    }

    @Test
    @DisplayName("PUT /v1/checklists/{checkListId} - success 200 no content")
    void updateCheckList_HappyPath() throws Exception {
        CheckList toUpdate = new CheckList();
        toUpdate.setJobId("job1");
        toUpdate.setDay(1);
        toUpdate.setShift("shift1");

        // We do not mock getOrganizationId, but controller calls static method getOrganizationId(request)
        // So we simulate header that could be used there if relevant, but since it's static utility, it's out of scope for mocking.

        String json = toJson(toUpdate);

        mockMvc.perform(put(BASE_URL + "/id1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("organizationId", "org1")  // simulate header, controller reads from request header internally
                        .content(json))
                .andExpect(status().isOk());

        // CheckList argument passed to updateCheckList must have organizationId set by controller
        // We cannot capture easily here due to static method, so verify service called once
        verify(checkListService, times(1)).updateCheckList(any(CheckList.class));
    }

    @Test
    @DisplayName("PUT /v1/checklists/{checkListId} - failure 500 on service exception")
    void updateCheckList_ServiceThrows_InternalServerError() throws Exception {
        CheckList toUpdate = new CheckList();
        toUpdate.setJobId("job1");
        toUpdate.setDay(1);
        toUpdate.setShift("shift1");

        String json = toJson(toUpdate);

        Mockito.doThrow(new RuntimeException("Service failure")).when(checkListService).updateCheckList(any());

        mockMvc.perform(put(BASE_URL + "/id1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("organizationId", "org1")
                        .content(json))
                .andExpect(status().isInternalServerError());

        verify(checkListService, times(1)).updateCheckList(any(CheckList.class));
    }

    @Test
    @DisplayName("POST /v1/checklists/ - success 200 no content")
    void saveCheckList_HappyPath() throws Exception {
        CheckList toSave = new CheckList();
        toSave.setJobId("job1");
        toSave.setDay(1);
        toSave.setShift("shift1");

        String json = toJson(toSave);

        Mockito.when(checkListService.saveCheckList(any())).thenReturn(toSave);

        mockMvc.perform(post(BASE_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("organizationId", "org1")
                        .content(json))
                .andExpect(status().isOk());

        verify(checkListService, times(1)).saveCheckList(any(CheckList.class));
    }

    @Test
    @DisplayName("POST /v1/checklists/ - failure 500 on service exception")
    void saveCheckList_ServiceThrows_InternalServerError() throws Exception {
        CheckList toSave = new CheckList();
        toSave.setJobId("job1");
        toSave.setDay(1);
        toSave.setShift("shift1");

        String json = toJson(toSave);

        Mockito.when(checkListService.saveCheckList(any())).thenThrow(new RuntimeException("Service failure"));

        mockMvc.perform(post(BASE_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("organizationId", "org1")
                        .content(json))
                .andExpect(status().isInternalServerError());

        verify(checkListService, times(1)).saveCheckList(any(CheckList.class));
    }

    @Test
    @DisplayName("DELETE /v1/checklists/{checkListId} - success 204 no content")
    void deleteCheckList_HappyPath() throws Exception {
        Mockito.doNothing().when(checkListService).deleteCheckList(any());

        mockMvc.perform(delete(BASE_URL + "/id1"))
                .andExpect(status().isNoContent());

        verify(checkListService, times(1)).deleteCheckList("id1");
    }

    @Test
    @DisplayName("DELETE /v1/checklists/{checkListId} - failure 500 on service exception")
    void deleteCheckList_ServiceThrows_InternalServerError() throws Exception {
        Mockito.doThrow(new RuntimeException("Service failure")).when(checkListService).deleteCheckList(any());

        mockMvc.perform(delete(BASE_URL + "/id1"))
                .andExpect(status().isInternalServerError());

        verify(checkListService, times(1)).deleteCheckList("id1");
    }
}