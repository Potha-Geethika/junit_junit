package com.carbo.admin.repository;

import com.carbo.admin.model.District;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictMongoDbRepository extends MongoRepository<District, String> {
    List<District> findByOrganizationId(String organizationId);
}
