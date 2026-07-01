package com.ngo.ngoservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ngo.ngoservice.model.NGO;

public interface NGORepository extends MongoRepository<NGO, String> {
    Optional<NGO> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    List<NGO> findByVerified(boolean verified);
}
