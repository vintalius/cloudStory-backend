package com.cloudstory.backend.util;

import java.security.MessageDigest;

public class PasswordUtil {

    // Cosmic/HeavenMS usually use SHA-512. 
    // If you use v83 OdinMS, change this to "SHA-1".
    private static final String ALGORITHM = "SHA-512"; 

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
