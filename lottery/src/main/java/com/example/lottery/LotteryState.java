// src/main/java/com/example/lottery/LotteryState.java
package com.example.lottery;

import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.repository.LotteryHistoryRepository;

import java.util.List;

public class LotteryState {
    private int totalDraws;
    private int sinceLastFourStar;
    private int sinceLastFiveStar;
    private FiveStarType lastFiveStar;

    public enum FiveStarType {
        NORMAL,
        LIMITED
    }

    private final LotteryHistoryRepository lotteryHistoryRepository;

    public LotteryState(LotteryHistoryRepository lotteryHistoryRepository) {
        this.lotteryHistoryRepository = lotteryHistoryRepository;
        loadHistory();
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

    public void loadHistory() {
        List<LotteryHistory> history = lotteryHistoryRepository.findAll();
        totalDraws = history.size();
        if (!history.isEmpty()) {
            LotteryHistory lastHistory = history.get(history.size() - 1);
            sinceLastFourStar = calculateSinceLastStar(history, 4);
            sinceLastFiveStar = calculateSinceLastStar(history, 5);
            lastFiveStar = lastHistory.getFiveStarType() != null ?
                FiveStarType.valueOf(lastHistory.getFiveStarType().toUpperCase()) : null;
        }
    }

    private int calculateSinceLastStar(List<LotteryHistory> history, int rarity) {
        int count = 0;
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).getRarity() == rarity) {
                break;
            }
            count++;
        }
        return count;
    }
}