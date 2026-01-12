package com.cloudstory.backend.service;

import com.cloudstory.backend.dto.PendingNxDTO;
import com.cloudstory.backend.entity.PendingNx;
import com.cloudstory.backend.repository.PendingNxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PendingNxService {

    @Autowired
    private PendingNxRepository pendingNxRepository;

    /**
     * Get all pending (unapplied) rewards for a user
     * @param username The username
     * @return List of pending rewards
     */
    public List<PendingNxDTO> getPendingRewards(String username) {
        List<PendingNx> pendingList = pendingNxRepository.findByUsernameAndApplied(username, false);
        
        return pendingList.stream()
            .map(p -> new PendingNxDTO(
                p.getId(),
                p.getUsername(),
                p.getNxCredit(),
                p.getVotePoints(),
                p.getRewardType(),
                p.getRewardSite(),
                p.getCreatedAt(),
                p.getApplied()
            ))
            .collect(Collectors.toList());
    }
}
