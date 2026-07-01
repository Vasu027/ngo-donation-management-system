package com.yourpackage.auth_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.yourpackage.auth_service.model.User;
import com.yourpackage.auth_service.model.Role;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    List<User> findByRoleAndVerified(Role role, boolean verified);
}
