package com.carbo.admin.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.model.Error;
import com.carbo.admin.model.Organization;
import com.carbo.admin.model.Role;
import com.carbo.admin.model.User;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponseDTO;
import com.carbo.admin.repository.UserMongoDbRepository;

@Service
public class MigrateUsersService {
    private final UserMongoDbRepository userRepository;

    private final MongoTemplate mongoTemplate;

    public MigrateUsersService(UserMongoDbRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<AiUser> saveAiUsersAndCollectUnsaved(List<AiUser> aiUsers) {
        try {
            List<User> all = userRepository.findAll();
            Set<String> tenantsName = aiUsers.stream().map(AiUser::getTenantName).collect(Collectors.toSet());
            String normalizedSearch = tenantsName.stream().map(name -> name.trim())  // Normalize spaces in each name
                                                 .collect(Collectors.joining("|", "(?i)^(", ")$"));
            Query query = new Query();
            //            query.addCriteria(Criteria.where("name").in("(?i)^" + tenantsName + "$"));
            query.addCriteria(Criteria.where("name").regex(normalizedSearch));
            List<Organization> organizationList = mongoTemplate.find(query, Organization.class);
            Set<String> existingUserNames = all.stream().map(User::getUserName).collect(Collectors.toSet());
            List<AiUser> existingUsers = new ArrayList<>();
            List<User> toBeSavedUsers = new ArrayList<>();
            for (AiUser aiUser : aiUsers) {
                if (existingUserNames.stream().filter(Objects::nonNull)
                                     .anyMatch(ex -> ex.equalsIgnoreCase(aiUser.getEmailAddress()))) { // Convert email address to lowercase
                    aiUser.setStatus("User not created : Username already exists in db");
                    existingUsers.add(aiUser);
                    continue;
                }
                if (!organizationList.isEmpty()) {
                    Optional<Organization> organization = organizationList.stream()
                                                                          .filter(org -> org.getName().equalsIgnoreCase(aiUser.getTenantName()))
                                                                          .findFirst();
                    User user = convertAiUserToOpsUser(aiUser);
                    user.setOrganizationId(organization.isPresent() ? organization.get().getId() : null);
                    toBeSavedUsers.add(user);
                    existingUserNames.add(user.getUserName().toLowerCase());
                } else {
                    aiUser.setStatus("User not created : Organization is not found with the tenant name");
                    existingUsers.add(aiUser);
                }
            }
            userRepository.saveAll(toBeSavedUsers);
            return existingUsers;
        } catch (Exception e) {
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage("Some Error occurred : " + e.getMessage())
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    private User convertAiUserToOpsUser(AiUser aiUser) {
        try {
            ModelMapper mapper = new ModelMapper();
            mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
            mapper.createTypeMap(AiUser.class, User.class).addMapping(AiUser::getEmailAddress, User::setUserName)
                  .addMapping(AiUser::getName, User::setFirstName).addMapping(AiUser::getSurname, User::setLastName)
                  .addMapping(AiUser::getAzureUserId, User::setAzureId).addMappings(mapping -> mapping.skip(User::setDistrictId))
                  .addMappings(mapping -> mapping.skip(User::setId)).addMappings(mapping -> mapping.skip(User::setOrganizationId));
            User opsUser = mapper.map(aiUser, User.class);
            opsUser.setCreatedBy("CreatedBy-AIUser");
            opsUser.setCreated(Instant.now().toEpochMilli());
            List<Role> list = new ArrayList<>();
            opsUser.setAuthorities(list);
            opsUser.setLastModifiedBy("ModifiedBy-AIUser");
            opsUser.setModified(Instant.now().toEpochMilli());
            opsUser.setTitle("Default-AIUser");
            opsUser.setStatus("Active");
            return opsUser;
        } catch (Exception e) {
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage("Some Error occurred : " + e.getMessage())
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }

    public List<UserResponseDTO> getAllUsersForAi() {
        List<UserResponseDTO> allUsers = userRepository.findAllUsersWithSelectedFields();
        List<Organization> organizations = mongoTemplate.findAll(Organization.class);
        for (UserResponseDTO user : allUsers) {
            for (Organization org : organizations) {
                if (org.getId().equals(user.getOrganizationId())) {
                    user.setOrganizationName(org.getName());
                }
            }
        }
        return allUsers;
    }

    public List<User> setUserAzureId(List<AiUser> users) {
        try {
            List<User> retrievedUsers = userRepository.findAll();
            List<User> updatedUsers = new ArrayList<>();
            for (AiUser user : users) {
                for (User retrievedUser : retrievedUsers) {
                    if (user.getEmailAddress().equalsIgnoreCase(retrievedUser.getUserName())) {
                        retrievedUser.setAzureId(user.getAzureUserId());
                        userRepository.save(retrievedUser);
                        updatedUsers.add(retrievedUser);
                    }
                }
            }
            return updatedUsers;
        } catch (Exception e) {
            Error error = Error.builder().errorCode(HttpStatus.BAD_REQUEST.toString()).errorMessage("Some Error occurred : " + e.getMessage())
                               .httpStatus(HttpStatus.BAD_REQUEST).build();
            throw new ErrorException(error);
        }
    }
}

