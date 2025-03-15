// src/main/java/com/example/lottery/entity/LotteryHistory.java
package com.example.lottery.entity;

import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "lottery_history")
public class LotteryHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "draw_time")
    private ZonedDateTime drawTime;

    @Column(name = "prize_name")
    private String prizeName;

    @Column(name = "rarity")
    private int rarity;

    @Column(name = "five_star_type")
    private String fiveStarType;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "prize_id", nullable = false)
    private Long prizeId;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(ZonedDateTime drawTime) {
        this.drawTime = drawTime;
    }

    public String getPrizeName() {
        return prizeName;
    }

    public void setPrizeName(String prizeName) {
        this.prizeName = prizeName;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(Long prizeId) {
        this.prizeId = prizeId;
    }
}