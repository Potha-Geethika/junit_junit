package com.carbo.checklist.services;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.checklist.model.CheckList;
import com.carbo.checklist.repository.CheckListMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;





@ExtendWith(MockitoExtension.class)
class CheckListServiceTest {

    @Mock
    private CheckListMongoDbRepository checkListRepository;

    @InjectMocks
    private CheckListService checkListService;

    private CheckList sampleCheckList;

    @BeforeEach
    void setUp() {
        sampleCheckList = new CheckList();
        sampleCheckList.setId("checklist1");
        sampleCheckList.setJobId("job1");
        sampleCheckList.setOrganizationId("org1");
        sampleCheckList.setShift("shift1");
        sampleCheckList.setDay(1);
        sampleCheckList.setLocked(false);
    }

    // getAll() tests

    @Test
    void getAll_returnsListOfCheckLists() {
        List<CheckList> expected = List.of(sampleCheckList);
        doReturn(expected).when(checkListRepository).findAll();

        List<CheckList> actual = checkListService.getAll();

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(checkListRepository, times(1)).findAll();
    }

    @Test
    void getAll_returnsEmptyListWhenNoCheckLists() {
        doReturn(Collections.emptyList()).when(checkListRepository).findAll();

        List<CheckList> actual = checkListService.getAll();

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(checkListRepository, times(1)).findAll();
    }

    // getByJobId(String) tests

    @Test
    void getByJobId_returnsListOfCheckLists() {
        List<CheckList> expected = List.of(sampleCheckList);
        doReturn(expected).when(checkListRepository).findByJobId("job1");

        List<CheckList> actual = checkListService.getByJobId("job1");

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(checkListRepository, times(1)).findByJobId("job1");
    }

    @Test
    void getByJobId_returnsEmptyListWhenNoCheckListsForJob() {
        doReturn(Collections.emptyList()).when(checkListRepository).findByJobId("job1");

        List<CheckList> actual = checkListService.getByJobId("job1");

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(checkListRepository, times(1)).findByJobId("job1");
    }

    @Test
    void getByJobId_withNullJobId_returnsEmptyList() {
        doReturn(Collections.emptyList()).when(checkListRepository).findByJobId(null);

        List<CheckList> actual = checkListService.getByJobId(null);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(checkListRepository, times(1)).findByJobId(null);
    }

    // getCheckList(String) tests

    @Test
    void getCheckList_returnsOptionalWithCheckList() {
        doReturn(Optional.of(sampleCheckList)).when(checkListRepository).findById("checklist1");

        Optional<CheckList> actual = checkListService.getCheckList("checklist1");

        assertNotNull(actual);
        assertTrue(actual.isPresent());
        assertEquals(sampleCheckList, actual.get());
        verify(checkListRepository, times(1)).findById("checklist1");
    }

    @Test
    void getCheckList_returnsEmptyOptionalWhenNotFound() {
        doReturn(Optional.empty()).when(checkListRepository).findById("unknownId");

        Optional<CheckList> actual = checkListService.getCheckList("unknownId");

        assertNotNull(actual);
        assertFalse(actual.isPresent());
        verify(checkListRepository, times(1)).findById("unknownId");
    }

    @Test
    void getCheckList_withNullId_returnsEmptyOptional() {
        doReturn(Optional.empty()).when(checkListRepository).findById(null);

        Optional<CheckList> actual = checkListService.getCheckList(null);

        assertNotNull(actual);
        assertFalse(actual.isPresent());
        verify(checkListRepository, times(1)).findById(null);
    }

    // saveCheckList(CheckList) tests

    @Test
    void saveCheckList_savesAndReturnsCheckList() {
        doReturn(sampleCheckList).when(checkListRepository).save(sampleCheckList);

        CheckList actual = checkListService.saveCheckList(sampleCheckList);

        assertNotNull(actual);
        assertEquals(sampleCheckList, actual);
        verify(checkListRepository, times(1)).save(sampleCheckList);
    }

    @Test
    void saveCheckList_withNullCheckList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> checkListService.saveCheckList(null));
        verify(checkListRepository, never()).save(any());
    }

    // updateCheckList(CheckList) tests

    @Test
    void updateCheckList_callsSaveOnRepository() {
        doReturn(sampleCheckList).when(checkListRepository).save(sampleCheckList);

        checkListService.updateCheckList(sampleCheckList);

        verify(checkListRepository, times(1)).save(sampleCheckList);
    }

    @Test
    void updateCheckList_withNullCheckList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> checkListService.updateCheckList(null));
        verify(checkListRepository, never()).save(any());
    }

    // deleteCheckList(String) tests

    @Test
    void deleteCheckList_callsDeleteById() {
        doNothing().when(checkListRepository).deleteById("checklist1");

        checkListService.deleteCheckList("checklist1");

        verify(checkListRepository, times(1)).deleteById("checklist1");
    }

    @Test
    void deleteCheckList_withNullId_callsDeleteByIdWithNull() {
        doNothing().when(checkListRepository).deleteById(null);

        checkListService.deleteCheckList(null);

        verify(checkListRepository, times(1)).deleteById(null);
    }
}