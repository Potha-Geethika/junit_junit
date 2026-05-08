package com.carbo.pad.repository;

import com.carbo.pad.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobMongoDbRepository extends MongoRepository<Job, String> {
    Optional<Job> findByIdAndOrganizationId(String id, String organizationId);

    Optional<Job> findByIdAndSharedWithOrganizationId(String id, String sharedWithOrganizationId);
}