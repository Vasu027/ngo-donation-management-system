package com.ngo.ngoservice.controller;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ngo.ngoservice.model.NGO;
import com.ngo.ngoservice.service.NGOService;

@RestController
@RequestMapping("/ngos")
public class NGOController {
    private final NGOService ngoService;

    public NGOController(NGOService ngoService) {
        this.ngoService = ngoService;
    }

    // ✅ Get all NGOs (Filtered by verification status for public/donors, unfiltered for Admin)
    @GetMapping
    public List<NGO> getAllNGOs(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if ("ADMIN".equals(role)) {
            return ngoService.getAllNGOs();
        }
        return ngoService.getVerifiedNGOs();
    }

    // ✅ Get NGO by database ID
    @GetMapping("/{id}")
    public ResponseEntity<NGO> getNGOById(@PathVariable String id) {
        return ngoService.getNGOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Get NGO by registration number
    @GetMapping("/reg/{regNo}")
    public ResponseEntity<NGO> getNGOByRegNo(@PathVariable String regNo) {
        return ngoService.getNGOByRegistrationNumber(regNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Create NGO (Force pending verification unless Admin)
    @PostMapping
    public ResponseEntity<?> createNGO(
            @RequestBody NGO ngo,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        if (role == null || (!"NGO".equals(role) && !"ADMIN".equals(role))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only NGO users or Admins can register NGO profiles.");
        }

        // Force verified to false for regular NGO registration
        if (!"ADMIN".equals(role)) {
            ngo.setVerified(false);
        }

        try {
            NGO savedNgo = ngoService.createNGO(ngo);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedNgo);
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Registration number already exists: " + ngo.getRegistrationNumber());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        }
    }

    // ✅ Update NGO (Only owner NGO or Admin)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNGO(
            @PathVariable String id,
            @RequestBody NGO ngo,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        if (role == null || (!"NGO".equals(role) && !"ADMIN".equals(role))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Unauthorized to update NGO profile.");
        }

        return ResponseEntity.ok(ngoService.updateNGO(id, ngo));
    }

    // ✅ Delete NGO (Admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNGO(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only administrators can delete NGO profiles.");
        }

        ngoService.deleteNGO(id);
        return ResponseEntity.ok("NGO with id " + id + " deleted successfully");
    }

    // ✅ Verify NGO by registration number (Called internally by auth-service)
    @PutMapping("/verify/reg/{regNo}")
    public ResponseEntity<?> verifyNGOByRegNo(@PathVariable String regNo) {
        try {
            NGO verified = ngoService.verifyNGOByRegNo(regNo);
            return ResponseEntity.ok(verified);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
