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
    private Integer rarity;

    @Column
    private String fiveStarType;

    @Column
    private String description;

    @Column(name = "is_repeatable", nullable = false)
    private Boolean isRepeatable = true; // 默认为 TRUE（可重复）

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true; // 默认新添加的奖品为启用状态

    // Constructors, Getters, and Setters
    public Prize() {}

    public Prize(String name, int rarity, String fiveStarType, String description) {
        this.name = name;
        this.rarity = rarity;
        this.fiveStarType = fiveStarType;
        this.description = description;
        this.isRepeatable = true;
        this.enabled = true;
    }

    public Prize(String name, int rarity, String fiveStarType, String description, Boolean isRepeatable) {
        this.name = name;
        this.rarity = rarity;
        this.fiveStarType = fiveStarType;
        this.description = description;
        this.isRepeatable = isRepeatable;
        this.enabled = true;
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

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRarity() {
        return rarity;
    }

    public void setRarity(Integer rarity) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
