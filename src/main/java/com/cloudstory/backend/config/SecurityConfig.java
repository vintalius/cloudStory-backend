package com.cloudstory.backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Use SHA-512 PasswordEncoder (same as registration)
        return new SHA512PasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
            .csrf(csrf -> csrf.disable()) // Disable CSRF for Registration to work
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless (JWT)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Add JWT Filter
            .authorizeHttpRequests(auth -> auth
                // 1. Public endpoints - accessible to everyone
                .requestMatchers("/", "/index.html").permitAll() // Root and index
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/forgot-password").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/rankings/**").permitAll()
                .requestMatchers("/api/status/**").permitAll()
                .requestMatchers("/api/debug/**").permitAll() // Debug endpoints
                // 2. Swagger/OpenAPI is public
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/*").permitAll()
                .requestMatchers("/v3/api-docs/**", "/v3/api-docs", "/api-docs/**", "/api-docs").permitAll()
                .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()
                // 3. Protected endpoints - require JWT authentication
                .requestMatchers("/api/user/**").authenticated()
                // 4. Everything else requires authentication
                .anyRequest().authenticated()
            );
            // Remove .httpBasic() - we're using JWT now, not Basic Auth

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",          // Local React development
            "http://localhost",               // Local without port
            "http://localhost:3000",          // Local production build
            "http://127.0.0.1:5173",          // 127.0.0.1 React development
            "http://127.0.0.1:3000",          // 127.0.0.1 production
            "http://72.60.127.22",            // VPS IP HTTP
            "http://72.60.127.22:8080",       // VPS with port 8080
            "http://cloudstory.online",       // Domain HTTP
            "https://cloudstory.online",      // Domain HTTPS
            "https://www.cloudstory.online"   // Domain HTTPS with www
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


