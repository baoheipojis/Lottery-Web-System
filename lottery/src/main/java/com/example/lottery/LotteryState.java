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

    // 抽奖保底系统相关计数器
    private int noFourStarCount = 0;  // 连续未获得四星的次数
    private int noFiveStarCount = 0;  // 连续未获得五星的次数

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
        if (amount <= 0) {
            throw new IllegalArgumentException("添加的计划点数必须为正数");
        }

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

    /**
     * 消费计划点，用于购买抽奖券或取消完成计划
     * @param points 要消费的点数
     * @param reason 消费原因
     * @return 消费后的余额
     */
    public int consumePlanPoints(int points, String reason) {
        // 打印原因字符串以便调试
        System.out.println("消费计划点，原因: " + reason + ", 数量: " + points);
        
        // 检查是否是取消完成计划操作（使用更可靠的检查方式）
        boolean isUncompletePlan = reason != null && 
            (reason.contains("取消完成计划") || reason.contains("取消完成习惯"));
        
        // 输出检查结果
        System.out.println("是否是取消完成操作: " + isUncompletePlan);
        
        // 如果不是取消完成计划/习惯操作，则验证余额是否足够
        if (!isUncompletePlan && getCurrentPlanPoints() < points) {
            throw new IllegalStateException("计划点不足，当前点数: " + getCurrentPlanPoints() + "，需要点数: " + points);
        }

        // 记录消费
        PlanPointsRecord record = new PlanPointsRecord();
        record.setAmountChange(-points);
        record.setTimestamp(LocalDateTime.now());
        record.setBalanceAfterOperation(getCurrentPlanPoints() - points);
        record.setDescription(reason);
        planPointsRecordRepository.save(record);

        return getCurrentPlanPoints() - points;
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

    // 抽奖保底系统相关方法

    /**
     * 获取连续未获得四星的次数
     */
    public int getNoFourStarCount() {
        return noFourStarCount;
    }

    /**
     * 获取连续未获得五星的次数
     */
    public int getNoFiveStarCount() {
        return noFiveStarCount;
    }

    /**
     * 增加连续未获得四星的计数
     */
    public void incrementNoFourStarCount() {
        noFourStarCount++;
    }

    /**
     * 增加连续未获得五星的计数
     */
    public void incrementNoFiveStarCount() {
        noFiveStarCount++;
    }

    /**
     * 重置连续未获得四星的计数
     */
    public void resetNoFourStarCount() {
        noFourStarCount = 0;
    }

    /**
     * 重置连续未获得五星的计数
     */
    public void resetNoFiveStarCount() {
        noFiveStarCount = 0;
    }
}