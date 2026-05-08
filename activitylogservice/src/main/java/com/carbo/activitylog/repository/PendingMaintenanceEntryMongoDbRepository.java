package com.carbo.activitylog.repository;

import com.carbo.activitylog.model.PendingMaintenanceEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingMaintenanceEntryMongoDbRepository extends MongoRepository<PendingMaintenanceEntry, String> {
    List<PendingMaintenanceEntry> findByOrganizationId(String organizationId);
    @Query("{ '_id': { $in: ?0 } }")
    void deleteByIds(List<String> ids);
}
