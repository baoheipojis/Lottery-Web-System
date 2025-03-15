package com.example.lottery.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "prizes")
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int rarity;

    @Column
    private String fiveStarType;

    @Column
    private String description;

    @Column(name = "is_repeatable", nullable = false)
    private Boolean isRepeatable = true; // 默认为 TRUE（可重复）

    // Constructors, Getters, and Setters
    public Prize() {}

    public Prize(String name, int rarity, String fiveStarType, String description) {
        this.name = name;
        this.rarity = rarity;
        this.fiveStarType = fiveStarType;
        this.description = description;
        this.isRepeatable = true;
    }
    public Prize(String name, int rarity, String fiveStarType, String description, Boolean isRepeatable) {
        this.name = name;
        this.rarity = rarity;
        this.fiveStarType = fiveStarType;
        this.description = description;
        this.isRepeatable = isRepeatable;
    }
    public Boolean getIsRepeatable() {
        return isRepeatable;
    }

    public void setIsRepeatable(Boolean isRepeatable) {
        this.isRepeatable = isRepeatable;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public String getFiveStarType() {
        return fiveStarType;
    }

    public void setFiveStarType(String fiveStarType) {
        this.fiveStarType = fiveStarType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
