package com.cloudstory.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudstory.backend.dto.UserResponse;
import com.cloudstory.backend.entity.Account;
import com.cloudstory.backend.repository.AccountRepository;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AccountRepository accountRepository;

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
                    account.getPaypalNX() != null ? account.getPaypalNX() : 0,
                    account.getMPoints() != null ? account.getMPoints() : 0,
                    account.getVPoints() != null ? account.getVPoints() : 0,
                    account.getCreatedAt()
            );

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }
}
