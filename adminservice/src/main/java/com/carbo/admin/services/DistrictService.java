package com.carbo.admin.services;

import com.carbo.admin.model.District;
import com.carbo.admin.repository.DistrictMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DistrictService {
    private final DistrictMongoDbRepository districtRepository;

    @Autowired
    public DistrictService(DistrictMongoDbRepository districtRepository) {
        this.districtRepository = districtRepository;
    }

    public List<District> getAll() {
        return districtRepository.findAll();
    }

    public List<District> getByOrganizationId(String organizationId) {
        return districtRepository.findByOrganizationId(organizationId);
    }

    public Optional<District> get(String districtId) {
        return districtRepository.findById(districtId);
    }

    public District save(District district) {
        return districtRepository.save(district);
    }

    public void update(District district) {
        districtRepository.save(district);
    }

    public void delete(String districtId) {
        districtRepository.deleteById(districtId);
    }
}
