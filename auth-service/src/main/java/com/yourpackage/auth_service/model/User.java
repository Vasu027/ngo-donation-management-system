package com.yourpackage.auth_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;
    
    private String email;

    private Role role;

    private String ngoRegNo; // only for NGO role
    private String ngoName;
    private String ngoCause;

    private boolean verified = true; // default true for DONOR and ADMIN, false for NGO

    public User() {}

    public User(String username, String password, String email, Role role, String ngoRegNo, boolean verified) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.ngoRegNo = ngoRegNo;
        this.verified = verified;
    }

    public User(String username, String password, String email, Role role, String ngoRegNo, String ngoName, String ngoCause, boolean verified) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.ngoRegNo = ngoRegNo;
        this.ngoName = ngoName;
        this.ngoCause = ngoCause;
        this.verified = verified;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getNgoRegNo() { return ngoRegNo; }
    public void setNgoRegNo(String ngoRegNo) { this.ngoRegNo = ngoRegNo; }
    public String getNgoName() { return ngoName; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }
    public String getNgoCause() { return ngoCause; }
    public void setNgoCause(String ngoCause) { this.ngoCause = ngoCause; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
