package com.carbo.activitylog.services;

import com.carbo.activitylog.model.ServiceAccount;
import com.carbo.activitylog.repository.ServiceAccountMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceAccountService {
    private final ServiceAccountMongoDbRepository serviceAccountMongoDbRepository;

    @Autowired
    public ServiceAccountService(ServiceAccountMongoDbRepository serviceAccountMongoDbRepository) {
        this.serviceAccountMongoDbRepository = serviceAccountMongoDbRepository;
    }

    public List<ServiceAccount> getAll() {
        return serviceAccountMongoDbRepository.findAll();
    }

    public List<ServiceAccount> getByOrganizationId(String organizationId) {
        return serviceAccountMongoDbRepository.findByOrganizationId(organizationId);
    }

    public Optional<ServiceAccount> get(String serviceAccountId) {
        return serviceAccountMongoDbRepository.findById(serviceAccountId);
    }

    public ServiceAccount save(ServiceAccount serviceAccount) {
        return serviceAccountMongoDbRepository.save(serviceAccount);
    }

    public void update(ServiceAccount serviceAccount) {
        serviceAccountMongoDbRepository.save(serviceAccount);
    }

    public void delete(String serviceAccountId) {
        serviceAccountMongoDbRepository.deleteById(serviceAccountId);
    }
}
