package com.carbo.activitylog.repository;

import com.carbo.activitylog.model.Pad;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PadMongoDbRepository extends MongoRepository<Pad, String> {
    List<Pad> findByOrganizationId(String organizationId);
    Optional<Pad> findDistinctByOrganizationIdAndName(String organizationId, String name);
}
