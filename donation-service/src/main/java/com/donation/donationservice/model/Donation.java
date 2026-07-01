package com.donation.donationservice.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "donations")
public class Donation {

    @Id
    private String id;
    private String donorName;
    private double amount;
    private String ngoRegNo;  // Link donation to NGO
    private String ngoName;
    private LocalDateTime donationDate = LocalDateTime.now();

    public Donation() {}

    public Donation(String donorName, double amount, String ngoRegNo, String ngoName) {
        this.donorName = donorName;
        this.amount = amount;
        this.ngoRegNo = ngoRegNo;
        this.ngoName = ngoName;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNgoRegNo() { return ngoRegNo; }
    public void setNgoRegNo(String ngoRegNo) { this.ngoRegNo = ngoRegNo; }

    public String getNgoName() { return ngoName; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }

    public LocalDateTime getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDateTime donationDate) { this.donationDate = donationDate; }
}
