package com.carbo.activitylog.services;

import com.carbo.activitylog.model.User;
import com.carbo.activitylog.repository.UserMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserMongoDbRepository userRepository;

    @Autowired
    public UserService(UserMongoDbRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public List<User> getByOrganizationId(String organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }

    public Optional<User> getUser(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(String emailGroupId) {
        userRepository.deleteById(emailGroupId);
    }
}
