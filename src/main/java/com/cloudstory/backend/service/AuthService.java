package com.cloudstory.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloudstory.backend.dto.RegisterRequest;
import com.cloudstory.backend.entity.Account;
import com.cloudstory.backend.repository.AccountRepository;
import com.cloudstory.backend.util.PasswordUtil;

@Service
public class AuthService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    public String register(RegisterRequest request) {
        if (accountRepository.existsByName(request.getUsername())) {
            throw new RuntimeException("Username already taken.");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use.");
        }

        Account account = new Account();
        account.setName(request.getUsername());
        account.setEmail(request.getEmail());
        
        // Hash the password
        account.setPassword(PasswordUtil.hashPassword(request.getPassword()));
        
        // --- Hardcoded Defaults (כדי שהמשחק לא יקרוס) ---
        
        // 1. תאריך לידה (אם המשתמש לא שלח, נשים דיפולט 2000-01-01)
        if (request.getBirthday() != null) {
            account.setBirthday(request.getBirthday());
        } else {
            account.setBirthday("2000-01-01");
        }
        
        account.setLoggedin(0);
        account.setGm(0);
        account.setNxCredit(0);
        account.setMPoints(0);
        account.setVPoints(0);

        accountRepository.save(account);

        // Send welcome email
        String emailSubject = "Welcome to CloudStory!";
        String emailBody = "Hello " + account.getName() + ",\n\n" +
                         "Thank you for registering with CloudStory!\n\n" +
                         "Your account has been created successfully. You can now login and start your adventure.\n\n" +
                         "Welcome to the world of CloudStory!\n\n" +
                         "Best regards,\n" +
                         "CloudStory Team";

        try {
            emailService.sendEmail(account.getEmail(), emailSubject, emailBody);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
            // Don't throw exception, registration still succeeds
        }

        return "Account created successfully!";
    }
}
