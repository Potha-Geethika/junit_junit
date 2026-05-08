package com.carbo.activitylog.services;

import com.carbo.activitylog.model.Pad;
import com.carbo.activitylog.repository.PadMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PadService {
    private final PadMongoDbRepository padRepository;

    @Autowired
    public PadService(PadMongoDbRepository padRepository) {
        this.padRepository = padRepository;
    }

    public List<Pad> getAll() {
        return padRepository.findAll();
    }

    public List<Pad> getByOrganizationId(String organizationId) {
        return padRepository.findByOrganizationId(organizationId);
    }

    public Optional<Pad> getByName(String organizationId, String name) {
        return padRepository.findDistinctByOrganizationIdAndName(organizationId, name);
    }
}
