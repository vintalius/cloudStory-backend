package com.cloudstory.backend.controller;

import com.cloudstory.backend.entity.MapleCharacter;
import com.cloudstory.backend.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    @Autowired
    private CharacterRepository characterRepository;

    @GetMapping
    public List<MapleCharacter> getTop5Characters() {
        return characterRepository.findTop5ByOrderByLevelDescExpDesc();
    }
}
