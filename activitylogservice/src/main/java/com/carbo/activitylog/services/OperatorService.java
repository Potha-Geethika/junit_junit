package com.carbo.activitylog.services;

import com.carbo.activitylog.repository.OperatorMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OperatorService {
    private final OperatorMongoDbRepository operatorRepository;

    @Autowired
    public OperatorService(OperatorMongoDbRepository operatorRepository) {
        this.operatorRepository = operatorRepository;
    }

    public Boolean isShared(String sharedFromId, String sharedToId) {
        return !operatorRepository.findByOrganizationIdAndLinkedOrganizationId(sharedFromId, sharedToId).isEmpty();
    }
}
