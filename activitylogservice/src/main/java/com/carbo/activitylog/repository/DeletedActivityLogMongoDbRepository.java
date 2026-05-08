package com.carbo.activitylog.repository;

import com.carbo.activitylog.model.DeletedActivityLogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeletedActivityLogMongoDbRepository extends MongoRepository<DeletedActivityLogEntry, String> {
    List<DeletedActivityLogEntry> findByOrganizationId(String organizationId);
    List<DeletedActivityLogEntry> findByOrganizationIdAndJobIdAndWellAndStage(String organizationId, String jobId, String well, Float stage);
    List<DeletedActivityLogEntry> findByOrganizationIdAndJobId(String organizationId, String jobId);
    List<DeletedActivityLogEntry> findByOrganizationIdAndJobIdAndDay(String organizationId, String jobId, Integer day);
}
