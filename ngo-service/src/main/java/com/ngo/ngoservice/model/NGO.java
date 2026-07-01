package com.ngo.ngoservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ngos")
public class NGO {

    @Id
    private String id;
    
    private String name;
    
    @Indexed(unique = true)
    private String registrationNumber;

    private String cause;

    private boolean verified = false;

    public NGO() {}

    public NGO(String name, String registrationNumber, String cause) {
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.cause = cause;
        this.verified = false;
    }

    public NGO(String name, String registrationNumber, String cause, boolean verified) {
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.cause = cause;
        this.verified = verified;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getCause() { return cause; }
    public void setCause(String cause) { this.cause = cause; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
