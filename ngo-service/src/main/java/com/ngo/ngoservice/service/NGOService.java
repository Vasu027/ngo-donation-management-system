package com.ngo.ngoservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ngo.ngoservice.model.NGO;
import com.ngo.ngoservice.repository.NGORepository;

@Service
public class NGOService {
    private final NGORepository ngoRepository;

    public NGOService(NGORepository ngoRepository) {
        this.ngoRepository = ngoRepository;
    }

    public List<NGO> getAllNGOs() {
        return ngoRepository.findAll();
    }

    public Optional<NGO> getNGOById(String id) {
        return ngoRepository.findById(id);
    }

    public NGO createNGO(NGO ngo) {
        return ngoRepository.save(ngo);
    }

    public NGO updateNGO(String id, NGO updatedNGO) {
        return ngoRepository.findById(id)
                .map(existingNGO -> {
                    existingNGO.setName(updatedNGO.getName());
                    existingNGO.setRegistrationNumber(updatedNGO.getRegistrationNumber());
                    existingNGO.setCause(updatedNGO.getCause());
                    return ngoRepository.save(existingNGO);
                })
                .orElseThrow(() -> new RuntimeException("NGO not found with id " + id));
    }

    public void deleteNGO(String id) {
        ngoRepository.deleteById(id);
    }

    public Optional<NGO> getNGOByRegistrationNumber(String regNo) {
        return ngoRepository.findByRegistrationNumber(regNo);
    }

    public List<NGO> getVerifiedNGOs() {
        return ngoRepository.findByVerified(true);
    }

    public List<NGO> getUnverifiedNGOs() {
        return ngoRepository.findByVerified(false);
    }

    public NGO verifyNGOByRegNo(String regNo) {
        NGO ngo = ngoRepository.findByRegistrationNumber(regNo)
                .orElseThrow(() -> new RuntimeException("NGO not found with registration number " + regNo));
        ngo.setVerified(true);
        return ngoRepository.save(ngo);
    }
}
