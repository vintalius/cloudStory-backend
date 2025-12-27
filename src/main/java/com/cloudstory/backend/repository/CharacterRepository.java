package com.cloudstory.backend.repository;

import com.cloudstory.backend.entity.MapleCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<MapleCharacter, Integer> {
    
    // Automatically generates SQL: SELECT * FROM characters ORDER BY level DESC, exp DESC LIMIT 5
    List<MapleCharacter> findTop5ByOrderByLevelDescExpDesc();
    
    // Ranking query excluding admins (level 200 and specific names)
    @Query("SELECT c FROM MapleCharacter c WHERE c.level < 200 AND c.name NOT IN ('TalX') ORDER BY c.level DESC, c.exp DESC")
    List<MapleCharacter> findTop5ExcludingAdmins();
}
