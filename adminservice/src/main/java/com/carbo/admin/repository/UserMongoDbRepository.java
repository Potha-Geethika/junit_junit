package com.carbo.admin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.carbo.admin.model.User;
import com.carbo.admin.model.azureB2C.UserResponseDTO;

@Repository
public interface UserMongoDbRepository extends MongoRepository<User, String> {
    List<User> findByOrganizationId(String organizationId);

    @Query ("{ 'userName' : { $regex: '^?0$', $options: 'i' } }")
    Optional<User> findByUserNameIgnoreCase(String userName);

    @Query (value = "{}", fields = "{'firstName' : 1, 'lastName' : 1, 'userName' : 1, 'organizationId' : 1, 'organizationName' : 1, 'azureId' : 1}")
    List<UserResponseDTO> findAllUsersWithSelectedFields();

    Optional<User> findByAzureId(String azureUserId);
}
