package com.cloudstory.backend.repository;

import com.cloudstory.backend.entity.PendingNx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingNxRepository extends JpaRepository<PendingNx, Integer> {
    
    /**
     * Find all pending (unapplied) rewards for a specific username
     * @param username The username
     * @param applied False to get only unapplied rewards
     * @return List of pending rewards
     */
    List<PendingNx> findByUsernameAndApplied(String username, Boolean applied);
    
    /**
     * Find all pending rewards for a specific account ID
     * @param accountId The account ID
     * @param applied False to get only unapplied rewards
     * @return List of pending rewards
     */
    List<PendingNx> findByAccountIdAndApplied(Integer accountId, Boolean applied);
}
