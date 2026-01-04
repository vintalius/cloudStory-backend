package com.cloudstory.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudstory.backend.dto.ChangePasswordRequest;
import com.cloudstory.backend.dto.UserResponse;
import com.cloudstory.backend.entity.Account;
import com.cloudstory.backend.repository.AccountRepository;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            // Get the authenticated username
            String username = authentication.getName();

            // Fetch the Account from database
            Account account = accountRepository.findByName(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Map to UserResponse (no password)
            UserResponse userResponse = new UserResponse(
                    account.getId(),
                    account.getName(),
                    account.getEmail(),
                    account.getNxCredit() != null ? account.getNxCredit() : 0,
                    account.getMPoints() != null ? account.getMPoints() : 0,
                    account.getVPoints() != null ? account.getVPoints() : 0,
                    account.getCreatedAt()
            );

            System.out.println("DEBUG: UserResponse created - vPoints: " + userResponse.getVPoints());

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            Account account = accountRepository.findByName(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
            }

            // Check if new password is same as current password
            if (passwordEncoder.matches(request.getNewPassword(), account.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password must be different from current password"));
            }

            // Hash and save new password
            String newHashedPassword = passwordEncoder.encode(request.getNewPassword());
            account.setPassword(newHashedPassword);
            accountRepository.save(account);

            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error changing password: " + e.getMessage()));
        }
    }
}
