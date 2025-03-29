// src/main/java/com/example/lottery/LotteryState.java
package com.example.lottery;

import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.repository.LotteryHistoryRepository;
import com.example.lottery.repository.PlanPointsRecordRepository;
import com.example.lottery.entity.PlanPointsRecord;

import java.util.List;
import java.time.LocalDateTime;

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
    private final PlanPointsRecordRepository planPointsRecordRepository;

    public LotteryState(LotteryHistoryRepository lotteryHistoryRepository, PlanPointsRecordRepository planPointsRecordRepository) {
        this.lotteryHistoryRepository = lotteryHistoryRepository;
        this.planPointsRecordRepository = planPointsRecordRepository;
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

    public int getCurrentPlanPoints() {
        return planPointsRecordRepository.sumAllPlanPoints();
    }

    public void addPlanPoints(int amount, String description) {
        PlanPointsRecord record = new PlanPointsRecord();
        record.setAmountChange(amount);
        record.setTimestamp(LocalDateTime.now());
        record.setBalanceAfterOperation(getCurrentPlanPoints() + amount);
        record.setDescription(description);
        planPointsRecordRepository.save(record);
    }

    public void addPlanPoints(int amount) {
        addPlanPoints(amount, "添加计划点");
    }

    public void consumePlanPoints(int amount, String description) {
        if (getCurrentPlanPoints() < amount) {
            throw new IllegalStateException("计划点不足");
        }
        PlanPointsRecord record = new PlanPointsRecord();
        record.setAmountChange(-amount);
        record.setTimestamp(LocalDateTime.now());
        record.setBalanceAfterOperation(getCurrentPlanPoints() - amount);
        record.setDescription(description);
        planPointsRecordRepository.save(record);
    }

    public void consumePlanPoints(int amount) {
        consumePlanPoints(amount, "通过抽卡消耗（未附加奖品信息）");
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