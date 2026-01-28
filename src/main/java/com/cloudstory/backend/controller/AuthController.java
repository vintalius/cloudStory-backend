package com.cloudstory.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudstory.backend.dto.AuthResponse;
import com.cloudstory.backend.dto.ChangePasswordRequest;
import com.cloudstory.backend.dto.LoginRequest;
import com.cloudstory.backend.dto.RegisterRequest;
import com.cloudstory.backend.dto.ResetPasswordRequest;
import com.cloudstory.backend.entity.Account;
import com.cloudstory.backend.entity.PasswordResetToken;
import com.cloudstory.backend.repository.AccountRepository;
import com.cloudstory.backend.repository.PasswordResetTokenRepository;
import com.cloudstory.backend.service.AuthService;
import com.cloudstory.backend.service.EmailService;
import com.cloudstory.backend.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 1. Basic Validation
            if (request.getRecaptchaToken() == null || request.getRecaptchaToken().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Captcha validation is required."));
            }

            // 2. Verify with Google
            String verifyUrl = "https://www.google.com/recaptcha/api/siteverify?secret=" 
                               + recaptchaSecret + "&response=" + request.getRecaptchaToken();

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> googleResponse = restTemplate.getForObject(verifyUrl, Map.class);

            // 3. Check Result
            if (googleResponse == null || !Boolean.TRUE.equals(googleResponse.get("success"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Captcha verification failed. Please try again."));
            }

            String message = authService.register(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Generate JWT Token
            String token = jwtUtil.generateToken(request.getUsername());

            // Return token
            AuthResponse response = new AuthResponse(token, request.getUsername());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            // Find account by email
            Optional<Account> accountOptional = accountRepository.findByEmail(email);

            if (accountOptional.isEmpty()) {
                // Don't reveal if email exists for security
                return ResponseEntity.ok("If an account with that email exists, a password reset link will be sent");
            }

            Account account = accountOptional.get();

            // Create and save password reset token
            PasswordResetToken resetToken = new PasswordResetToken(account);
            passwordResetTokenRepository.save(resetToken);

            // Generate reset link using configured frontend URL (supports dev and production)
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getToken();

            // Send email
            String subject = "Password Reset Request";
            String body = "Click the link below to reset your password:\n\n" + resetLink + 
                         "\n\nThis link will expire in 24 hours.\n\nIf you did not request this, please ignore this email.";

            emailService.sendEmail(account.getEmail(), subject, body);

            return ResponseEntity.ok("If an account with that email exists, a password reset link will be sent");

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error processing password reset request: " + e.getMessage()));
        }
    }

    @PostMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
            }

            // Find the reset token
            Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);

            if (tokenOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired password reset token"));
            }

            PasswordResetToken resetToken = tokenOptional.get();

            // Check if token is expired
            if (resetToken.isExpired()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password reset token has expired"));
            }

            // Token is valid
            return ResponseEntity.ok(Map.of("message", "Token is valid"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error validating token: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String token = request.getToken();

            // Find the reset token
            Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);

            if (tokenOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired password reset token"));
            }

            PasswordResetToken resetToken = tokenOptional.get();

            // Check if token is expired
            if (resetToken.isExpired()) {
                passwordResetTokenRepository.delete(resetToken);
                return ResponseEntity.badRequest().body(Map.of("error", "Password reset token has expired"));
            }

            // Get the account and update password
            Account account = resetToken.getAccount();
            String hashedPassword = passwordEncoder.encode(request.getNewPassword());
            account.setPassword(hashedPassword);
            accountRepository.save(account);

            // Delete the used token
            passwordResetTokenRepository.delete(resetToken);

            return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now login with your new password."));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error resetting password: " + e.getMessage()));
        }
    }
}


