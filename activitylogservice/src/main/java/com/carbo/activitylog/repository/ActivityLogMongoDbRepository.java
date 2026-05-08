package com.carbo.activitylog.repository;

import com.carbo.activitylog.model.ActivityLogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityLogMongoDbRepository extends MongoRepository<ActivityLogEntry, String> {
    List<ActivityLogEntry> findByOrganizationId(String organizationId);
    List<ActivityLogEntry> findByOrganizationIdAndJobIdAndWellAndStage(String organizationId, String jobId, String well, Float stage);
    List<ActivityLogEntry> findByOrganizationIdAndJobId(String organizationId, String jobId);
    List<ActivityLogEntry> findByOrganizationIdAndJobIdAndDay(String organizationId, String jobId, Integer day);
    Optional<ActivityLogEntry> findByOrganizationIdAndJobIdAndDayAndWellAndStartAndEndAndStage(String organizationId, String jobId, Integer day, String well, String start, String end, Float stage);

    @Query(value = "{ 'organizationId' : ?0, 'created': { $gte: ?1, $lte: ?2 } }", fields = "{'id': 1, 'jobId': 1, 'organizationId': 1, 'created': 1}")
    List<ActivityLogEntry> getSimplifiedActivitiesByOrganizationIdAndCreatedRange(
            String organizationId, Long startEpochMillis, Long endEpochMillis);

    @Query(value = "{ 'created': { $gte: ?0, $lte: ?1 } }", fields = "{'id': 1, 'jobId': 1, 'organizationId': 1, 'created': 1}")
    List<ActivityLogEntry> getSimplifiedActivitiesForMismatchEntriesAndCreatedRange(
            Long startEpochMillis, Long endEpochMillis);

    List<ActivityLogEntry> findByOrganizationIdAndJobIdAndDayAndWell(String organizationId, String jobId, Integer day, String well);

    List<ActivityLogEntry> findByJobIdAndDay(String jobId, int copyDay);

    boolean existsByJobIdAndDay(String jobId, int day);

    Boolean existsByJobIdAndOrganizationId(String jobId, String organizationId);

    @Query("{ 'organizationId': ?0, 'jobId': ?1, 'opsActivity': { $regex: ?2, $options: 'i' } }")
    List<ActivityLogEntry> findByOrganizationIdAndJobIdAndOpsActivity(String organizationId, String jobId, String opsActivity);

    List<ActivityLogEntry> findByOrganizationIdAndJobIdAndWell(String organizationId, String jobId, String well);

    @Query("{ 'jobId': ?0, 'eventOrNptCode': ?1 }")
    List<ActivityLogEntry> findByJobIdAndEventOrNptCode(String jobId, String eventOrNptCode);

    List<ActivityLogEntry> findByJobId(String jobId);
}
