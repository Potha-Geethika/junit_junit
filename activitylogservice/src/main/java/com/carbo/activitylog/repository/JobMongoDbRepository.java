package com.carbo.activitylog.repository;

import com.carbo.activitylog.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMongoDbRepository extends MongoRepository<Job, String> {
    List<Job> findByOrganizationId(String organizationId);

    List<Job> findByOrganizationIdAndId(String organizationId, String jobId);

    List<Job> findByJobNumber(String jobNumber);

    @Query(value = "{}", fields = "{'id': 1, 'organizationId': 1}")
    List<Job> getSimplifiedJobForMismatchEntries();

    Optional<Job> findByIdAndSharedWithOrganizationId(String id, String sharedWithOrganizationId);

    Optional<Job> findByIdAndOrganizationId(String id, String organizationId);
}
