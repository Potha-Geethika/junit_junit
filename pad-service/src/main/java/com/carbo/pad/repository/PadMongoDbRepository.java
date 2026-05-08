package com.carbo.pad.repository;

import com.carbo.pad.model.Pad;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PadMongoDbRepository extends MongoRepository<Pad, String> {
    List<Pad> findByOrganizationId(String organizationId);

    List<Pad> findByOrganizationIdIn(Set<String> organizationIds);
}
