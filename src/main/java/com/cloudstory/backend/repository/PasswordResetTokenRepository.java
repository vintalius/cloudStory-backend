package com.cloudstory.backend.repository;

import com.cloudstory.backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find a password reset token by its token string
     *
     * @param token The token UUID string
     * @return Optional containing the PasswordResetToken if found
     */
    Optional<PasswordResetToken> findByToken(String token);
}
