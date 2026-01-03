package com.cloudstory.backend.repository;

import com.cloudstory.backend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    /**
     * Find the most recent vote by a specific user for a specific voting site
     * @param username The username
     * @param voteSite The voting site name (e.g., "gtop100", "topg")
     * @return The most recent vote if exists
     */
    Optional<Vote> findTopByUsernameAndVoteSiteOrderByVotedAtDesc(String username, String voteSite);

    /**
     * Find all votes by a specific user, ordered by most recent first
     * @param username The username
     * @return List of all votes by the user
     */
    List<Vote> findByUsernameOrderByVotedAtDesc(String username);

    /**
     * Count total votes by a specific user
     * @param username The username
     * @return Total number of votes
     */
    long countByUsername(String username);
}
