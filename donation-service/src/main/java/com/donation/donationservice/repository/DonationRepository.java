package com.donation.donationservice.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.donation.donationservice.model.Donation;

@Repository
public interface DonationRepository extends MongoRepository<Donation, String> {
    List<Donation> findByDonorName(String donorName);
    List<Donation> findByNgoRegNo(String ngoRegNo);  
    List<Donation> findByNgoName(String ngoName);    
}
