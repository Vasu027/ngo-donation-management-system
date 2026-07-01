package com.yourpackage.auth_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yourpackage.auth_service.model.User;
import com.yourpackage.auth_service.model.Role;
import com.yourpackage.auth_service.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initAdmin() {
        // Seed default Admin if database is empty or admin doesn't exist
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User(
                "admin",
                passwordEncoder.encode("admin123"),
                "admin@donationportal.org",
                Role.ADMIN,
                null,
                true
            );
            userRepository.save(admin);
            System.out.println("Default Admin account seeded successfully: admin/admin123");
        }
    }

    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }

        // Hash the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set verification status and validation based on Role
        if (user.getRole() == Role.NGO) {
            if (user.getNgoRegNo() == null || user.getNgoRegNo().trim().isEmpty()) {
                throw new RuntimeException("NGO Registration Number is required for NGO signup.");
            }
            user.setVerified(false); // NGO accounts must be verified by admin
        } else if (user.getRole() == Role.ADMIN) {
            // Block public signup of admins
            throw new RuntimeException("Public Admin registration is disabled.");
        } else {
            user.setRole(Role.DONOR);
            user.setVerified(true); // Donors verified by default
        }

        User savedUser = userRepository.save(user);

        // Automatically create a pending NGO profile in ngo-service
        if (savedUser.getRole() == Role.NGO) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> ngoProfile = new java.util.HashMap<>();
                ngoProfile.put("name", savedUser.getNgoName() != null && !savedUser.getNgoName().trim().isEmpty() ? savedUser.getNgoName() : savedUser.getUsername());
                ngoProfile.put("registrationNumber", savedUser.getNgoRegNo());
                ngoProfile.put("cause", savedUser.getNgoCause() != null && !savedUser.getNgoCause().trim().isEmpty() ? savedUser.getNgoCause() : "General");
                ngoProfile.put("verified", false);
                
                String jsonBody = mapper.writeValueAsString(ngoProfile);

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/ngos"))
                        .header("Content-Type", "application/json")
                        .header("X-User-Role", "ADMIN") // Call as ADMIN to allow profile creation
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 201) {
                    System.err.println("Failed to create NGO profile in ngo-service: " + response.body());
                } else {
                    System.out.println("Successfully created pending NGO profile in ngo-service for: " + savedUser.getNgoRegNo());
                }
            } catch (Exception e) {
                System.err.println("Error calling ngo-service to create profile: " + e.getMessage());
            }
        }

        return savedUser;
    }

    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password.");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Your NGO profile is pending verification by the administrator.");
        }

        return user;
    }

    public List<User> getPendingNGOs() {
        return userRepository.findByRoleAndVerified(Role.NGO, false);
    }

    public User approveNGO(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getRole() != Role.NGO) {
            throw new RuntimeException("User is not an NGO.");
        }

        user.setVerified(true);
        User savedUser = userRepository.save(user);

        // Propagate verification to ngo-service profile
        if (user.getNgoRegNo() != null) {
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/ngos/verify/reg/" + user.getNgoRegNo()))
                        .PUT(java.net.http.HttpRequest.BodyPublishers.noBody())
                        .build();
                java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    System.err.println("Failed to verify NGO profile in ngo-service: " + response.body());
                } else {
                    System.out.println("Successfully verified NGO profile in ngo-service for: " + user.getNgoRegNo());
                }
            } catch (Exception e) {
                System.err.println("Error calling ngo-service verification: " + e.getMessage());
            }
        }

        return savedUser;
    }
}
