package com.donation.donationservice.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.donation.donationservice.model.Donation;
import com.donation.donationservice.service.DonationService;

@RestController
@RequestMapping("/donations")
public class DonationController {

    private final DonationService donationService;
    private final RestTemplate restTemplate;

    public DonationController(DonationService donationService, RestTemplate restTemplate) {
        this.donationService = donationService;
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public List<Donation> getAllDonations(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @RequestHeader(value = "X-User-NgoRegNo", required = false) String ngoRegNo) {
        
        if ("ADMIN".equals(role)) {
            return donationService.getAllDonations();
        } else if ("NGO".equals(role)) {
            if (ngoRegNo == null || ngoRegNo.isEmpty()) {
                return List.of();
            }
            return donationService.getDonationsByNgo(ngoRegNo);
        } else if ("DONOR".equals(role)) {
            return donationService.getDonationsByDonor(username);
        }
        
        return List.of();
    }

    @PostMapping
    public ResponseEntity<?> addDonation(
            @RequestBody Donation donation,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        
        if (!"DONOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Donors can make donations.");
        }

        if (donation.getNgoRegNo() == null || donation.getNgoRegNo().isEmpty()) {
            return ResponseEntity.badRequest().body("ngoRegNo is required.");
        }

        // Bind donation to authenticated donor
        donation.setDonorName(username);

        // Call NGO service to validate regNo
        String ngoServiceUrl = "http://localhost:8080/ngos/reg/{regNo}";
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    ngoServiceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    donation.getNgoRegNo()
            );

            Map<String, Object> ngoDetails = response.getBody();
            if (ngoDetails == null) {
                return ResponseEntity.badRequest()
                        .body("NGO with regNo not found: " + donation.getNgoRegNo());
            }

            // set NGO name from response
            donation.setNgoName((String) ngoDetails.get("name"));

        } catch (HttpClientErrorException.NotFound nf) {
            return ResponseEntity.badRequest()
                    .body("NGO with regNo not found: " + donation.getNgoRegNo());
        } catch (RestClientException ex) {
            return ResponseEntity.status(500)
                    .body("Failed to validate NGO: " + ex.getMessage());
        }

        donation.setDonationDate(LocalDateTime.now());
        Donation saved = donationService.addDonation(donation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public Donation getDonationById(@PathVariable String id) {
        return donationService.getDonationById(id)
                .orElseThrow(() -> new RuntimeException("Donation not found with id " + id));
    }

    @GetMapping("/donor/{donorName}")
    public List<Donation> getDonationsByDonor(@PathVariable String donorName) {
        return donationService.getDonationsByDonor(donorName);
    }

    @GetMapping("/ngo/reg/{regNo}")
    public List<Donation> getDonationsByNgoRegNo(@PathVariable String regNo) {
        return donationService.getDonationsByNgo(regNo);
    }

    @GetMapping("/ngo/name/{ngoName}")
    public List<Donation> getDonationsByNgoName(@PathVariable String ngoName) {
        return donationService.getDonationsByNgoName(ngoName);
    }

    @PutMapping("/{id}")
    public Donation updateDonation(@PathVariable String id, @RequestBody Donation donation) {
        return donationService.updateDonation(id, donation);
    }

    @DeleteMapping("/{id}")
    public String deleteDonation(@PathVariable String id) {
        donationService.deleteDonation(id);
        return "Donation with id " + id + " deleted successfully!";
    }
}
