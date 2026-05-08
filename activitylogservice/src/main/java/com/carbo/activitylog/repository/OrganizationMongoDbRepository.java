package com.carbo.activitylog.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationMongoDbRepository extends MongoRepository<com.carbo.activitylog.model.Organization, String> {
}
