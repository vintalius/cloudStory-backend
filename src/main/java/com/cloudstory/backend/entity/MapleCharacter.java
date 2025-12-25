package com.cloudstory.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "characters") // Maps to SQL table 'characters'
public class MapleCharacter {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "accountid")
    private Integer accountId;

    @Column(name = "name")
    private String name;

    @Column(name = "level")
    private Integer level;

    @Column(name = "exp")
    private Integer exp;

    @Column(name = "job")
    private Integer job;

    @Column(name = "fame")
    private Integer fame;

    @Column(name = "str")
    private Integer str;

    @Column(name = "dex")
    private Integer dex;

    @Column(name = "luk")
    private Integer luk;
    
    @Column(name = "`int`") // We use ` ` because "int" is a reserved word in SQL
    private Integer intStat;
}
