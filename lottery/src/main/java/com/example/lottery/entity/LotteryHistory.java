package com.example.lottery.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lottery_history")
public class LotteryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 奖品结果描述，比如 "三星奖品"、"四星奖品"、"普通五星奖品" 或 "限定五星奖品"
    @Column(nullable = false)
    private String result;

    // 奖品稀有度：3、4、5；对于五星，我们再通过 fiveStarType 区分
    @Column(nullable = false)
    private int rarity;

    // 当 rarity 为 5 时，用来区分五星类型（例如 "normal" 或 "limited"，其它情况可为空）
    @Column
    private String fiveStarType;

    // 抽奖时间
    @Column(nullable = false)
    private LocalDateTime drawTime;

    public LotteryHistory() {
    }

    public LotteryHistory(String result, int rarity, String fiveStarType, LocalDateTime drawTime) {
        this.result = result;
        this.rarity = rarity;
        this.fiveStarType = fiveStarType;
        this.drawTime = drawTime;
    }

    // Getter 和 Setter

    public Long getId() {
        return id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public LocalDateTime getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(LocalDateTime drawTime) {
        this.drawTime = drawTime;
    }
}
