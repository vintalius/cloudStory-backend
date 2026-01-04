package com.cloudstory.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudstory.backend.entity.Account;
import com.cloudstory.backend.repository.AccountRepository;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        java.util.Optional<Account> user = accountRepository.findByName(username);
        if (user.isPresent()) {
            Account account = user.get();
            return ResponseEntity.ok(new Object() {
                public String username = account.getName();
                public String email = account.getEmail();
                public String storedPasswordHash = account.getPassword();
                public String hashOfTest123 = passwordEncoder.encode("Test123"); // BCrypt generates different salt each time
                public String doTheyMatch = passwordEncoder.matches("Test123", account.getPassword()) ? "YES" : "NO";
            });
        }
        return ResponseEntity.notFound().build();
    }
}
