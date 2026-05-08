package com.carbo.admin.services;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.admin.model.District;
import com.carbo.admin.repository.DistrictMongoDbRepository;
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
class DistrictServiceTest {

    @Mock
    private DistrictMongoDbRepository districtRepository;

    private DistrictService districtService;

    @BeforeEach
    void setUp() {
        districtService = new DistrictService(districtRepository);
    }

    // getAll - Happy Path
    @Test
    void getAll_ShouldReturnListOfDistricts() {
        List<District> expectedList = Collections.singletonList(new District());
        when(districtRepository.findAll()).thenReturn(expectedList);

        List<District> actualList = districtService.getAll();

        assertNotNull(actualList);
        assertEquals(expectedList.size(), actualList.size());
        verify(districtRepository, times(1)).findAll();
    }

    // getAll - Empty List
    @Test
    void getAll_ShouldReturnEmptyListWhenNoDistricts() {
        when(districtRepository.findAll()).thenReturn(Collections.emptyList());

        List<District> actualList = districtService.getAll();

        assertNotNull(actualList);
        assertTrue(actualList.isEmpty());
        verify(districtRepository, times(1)).findAll();
    }

    // getByOrganizationId - Happy Path
    @Test
    void getByOrganizationId_ShouldReturnListForOrganization() {
        String orgId = "org1";
        List<District> expectedList = Collections.singletonList(new District());
        when(districtRepository.findByOrganizationId(orgId)).thenReturn(expectedList);

        List<District> actualList = districtService.getByOrganizationId(orgId);

        assertNotNull(actualList);
        assertEquals(expectedList.size(), actualList.size());
        verify(districtRepository, times(1)).findByOrganizationId(orgId);
    }

    // getByOrganizationId - Null or Empty orgId
    @Test
    void getByOrganizationId_ShouldReturnEmptyListWhenOrgIdNull() {
        when(districtRepository.findByOrganizationId(null)).thenReturn(Collections.emptyList());

        List<District> actualList = districtService.getByOrganizationId(null);

        assertNotNull(actualList);
        assertTrue(actualList.isEmpty());
        verify(districtRepository, times(1)).findByOrganizationId(null);
    }

    @Test
    void getByOrganizationId_ShouldReturnEmptyListWhenOrgIdEmpty() {
        when(districtRepository.findByOrganizationId("")).thenReturn(Collections.emptyList());

        List<District> actualList = districtService.getByOrganizationId("");

        assertNotNull(actualList);
        assertTrue(actualList.isEmpty());
        verify(districtRepository, times(1)).findByOrganizationId("");
    }

    // get - Happy Path
    @Test
    void get_ShouldReturnOptionalDistrict() {
        String districtId = "d1";
        District district = new District();
        when(districtRepository.findById(districtId)).thenReturn(Optional.of(district));

        Optional<District> result = districtService.get(districtId);

        assertTrue(result.isPresent());
        assertEquals(district, result.get());
        verify(districtRepository, times(1)).findById(districtId);
    }

    // get - Optional empty when not found
    @Test
    void get_ShouldReturnEmptyOptionalWhenNotFound() {
        String districtId = "notfound";
        when(districtRepository.findById(districtId)).thenReturn(Optional.empty());

        Optional<District> result = districtService.get(districtId);

        assertFalse(result.isPresent());
        verify(districtRepository, times(1)).findById(districtId);
    }

    // save - Happy Path
    @Test
    void save_ShouldReturnSavedDistrict() {
        District district = new District();
        when(districtRepository.save(district)).thenReturn(district);

        District saved = districtService.save(district);

        assertNotNull(saved);
        assertEquals(district, saved);
        verify(districtRepository, times(1)).save(district);
    }

    // save - Null district (should throw NPE)
    @Test
    void save_ShouldThrowNPEWhenDistrictIsNull() {
        assertThrows(NullPointerException.class, () -> districtService.save(null));
        verify(districtRepository, never()).save(any());
    }

    // update - Happy Path
    @Test
    void update_ShouldCallSaveOnRepository() {
        District district = new District();

        districtService.update(district);

        verify(districtRepository, times(1)).save(district);
    }

    // update - Null district (should throw NPE)
    @Test
    void update_ShouldThrowNPEWhenDistrictIsNull() {
        assertThrows(NullPointerException.class, () -> districtService.update(null));
        verify(districtRepository, never()).save(any());
    }

    // delete - Happy Path
    @Test
    void delete_ShouldCallDeleteById() {
        String districtId = "d1";

        districtService.delete(districtId);

        verify(districtRepository, times(1)).deleteById(districtId);
    }

    // delete - Null districtId (should throw NPE)
    @Test
    void delete_ShouldThrowNPEWhenDistrictIdNull() {
        assertThrows(NullPointerException.class, () -> districtService.delete(null));
        verify(districtRepository, never()).deleteById(any());
    }
}