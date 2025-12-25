package com.cloudstory.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloudstory.backend.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    Optional<Account> findByName(String name);
    Optional<Account> findByEmail(String email);
    
    // Count online players (loggedin > 0)
    long countByLoggedinGreaterThan(int value);
}
