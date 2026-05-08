package com.carbo.activitylog.repository;

import com.carbo.activitylog.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMongoDbRepository extends MongoRepository<User, String> {
    List<User> findByOrganizationId(String organizationId);
    Optional<User> findByUserName(String userName);
}
