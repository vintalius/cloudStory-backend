package com.cloudstory.backend.repository;

import com.cloudstory.backend.entity.MapleCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<MapleCharacter, Integer> {
    
    // Automatically generates SQL: SELECT * FROM characters ORDER BY level DESC, exp DESC LIMIT 5
    List<MapleCharacter> findTop5ByOrderByLevelDescExpDesc();
}
