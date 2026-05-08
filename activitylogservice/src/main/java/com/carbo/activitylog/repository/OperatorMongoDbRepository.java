package com.carbo.activitylog.repository;

import com.carbo.activitylog.model.Operator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperatorMongoDbRepository extends MongoRepository<Operator, String> {
    List<Operator> findByOrganizationIdAndLinkedOrganizationId(String organizationId, String linkedOrganizationId);
}
