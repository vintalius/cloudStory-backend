package com.cloudstory.backend.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.cloudstory.backend.util.PasswordUtil;

public class SHA512PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // Hash the password using SHA-512
        String hash = PasswordUtil.hashPassword(rawPassword.toString());
        System.out.println("DEBUG: Encoding password '" + rawPassword + "' -> " + hash.substring(0, 20) + "...");
        return hash;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Hash the input and compare with stored hash
        String hashedInput = PasswordUtil.hashPassword(rawPassword.toString());
        boolean matches = hashedInput.equals(encodedPassword);
        System.out.println("DEBUG: Matching password '" + rawPassword + "'");
        System.out.println("  - Hashed input:    " + hashedInput.substring(0, 20) + "...");
        System.out.println("  - Stored hash:     " + encodedPassword.substring(0, 20) + "...");
        System.out.println("  - Match result:    " + matches);
        return matches;
    }
}
