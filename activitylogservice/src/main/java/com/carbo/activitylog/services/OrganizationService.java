package com.carbo.activitylog.services;

import com.carbo.activitylog.model.Organization;
import com.carbo.activitylog.repository.OrganizationMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {
    private final OrganizationMongoDbRepository organizationRepository;

    @Autowired
    public OrganizationService(OrganizationMongoDbRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public List<Organization> getAll() {
        return organizationRepository.findAll();
    }

    public Optional<Organization> get(String organizationId) {
        return organizationRepository.findById(organizationId);
    }

    public Organization save(Organization organization) {
        return organizationRepository.save(organization);
    }

    public void update(Organization organization) {
        organizationRepository.save(organization);
    }

    public void delete(String organizationId) {
        organizationRepository.deleteById(organizationId);
    }
}
