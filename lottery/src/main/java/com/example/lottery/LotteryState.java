package com.example.lottery;

// 玩家抽奖状态
public class LotteryState {
    private int totalDraws;           // 总抽奖次数
    private int sinceLastFourStar;    // 距离上次抽到四星的抽数
    private int sinceLastFiveStar;    // 距离上次抽到五星的抽数
    private FiveStarType lastFiveStar; // 上一次五星的类型（普通/限定）

    // 定义五星类型枚举
    public enum FiveStarType {
        NORMAL,    // 普通五星
        LIMITED    // 限定五星
    }

    public int getTotalDraws() {
        return totalDraws;
    }

    public void incrementTotalDraws() {
        totalDraws++;
    }

    public int getSinceLastFourStar() {
        return sinceLastFourStar;
    }

    public void incrementSinceLastFourStar() {
        sinceLastFourStar++;
    }

    public void resetSinceLastFourStar() {
        sinceLastFourStar = 0;
    }

    public int getSinceLastFiveStar() {
        return sinceLastFiveStar;
    }

    public void incrementSinceLastFiveStar() {
        sinceLastFiveStar++;
    }

    public void resetSinceLastFiveStar() {
        sinceLastFiveStar = 0;
    }

    public FiveStarType getLastFiveStar() {
        return lastFiveStar;
    }

    public void setLastFiveStar(FiveStarType lastFiveStar) {
        this.lastFiveStar = lastFiveStar;
    }
}
