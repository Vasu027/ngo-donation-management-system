package com.donation.donationservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.donation.donationservice.model.Donation;
import com.donation.donationservice.repository.DonationRepository;

@Service
public class DonationService {

    private final DonationRepository donationRepository;

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public Donation addDonation(Donation donation) {
        return donationRepository.save(donation);
    }

    public List<Donation> getDonationsByDonor(String donorName) {
        return donationRepository.findByDonorName(donorName);
    }

    public List<Donation> getDonationsByNgo(String ngoRegNo) {
        return donationRepository.findByNgoRegNo(ngoRegNo);
    }

    public List<Donation> getDonationsByNgoName(String ngoName) {
        return donationRepository.findByNgoName(ngoName);
    }

    public Optional<Donation> getDonationById(String id) {
        return donationRepository.findById(id);
    }

    public Donation updateDonation(String id, Donation updatedDonation) {
        return donationRepository.findById(id).map(donation -> {
            donation.setDonorName(updatedDonation.getDonorName());
            donation.setAmount(updatedDonation.getAmount());
            donation.setNgoRegNo(updatedDonation.getNgoRegNo());
            donation.setNgoName(updatedDonation.getNgoName());
            donation.setDonationDate(updatedDonation.getDonationDate());
            return donationRepository.save(donation);
        }).orElseThrow(() -> new RuntimeException("Donation not found with id " + id));
    }

    public void deleteDonation(String id) {
        donationRepository.deleteById(id);
    }
}
