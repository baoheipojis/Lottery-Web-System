package com.example.lottery;

import com.example.lottery.entity.Prize;
import com.example.lottery.strategy.LotteryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LotteryContext {
    private LotteryStrategy strategy;
    private static final int DRAW_COST = 160;

    @Autowired
    public LotteryContext(@Autowired @Qualifier("defaultLotteryStrategy") LotteryStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(LotteryStrategy strategy) {
        this.strategy = strategy;
    }

    public Prize executeDraw(LotteryState state) {
        // 先检查计划点是否足够
        if (state.getCurrentPlanPoints() < DRAW_COST) {
            throw new InsufficientPlanPointsException("计划点数不足，需要 " + DRAW_COST + " 点，当前只有 " + state.getCurrentPlanPoints() + " 点");
        }
        
        // 尝试获取奖品
        Prize prize = strategy.draw(state);
        
        // 如果没有获取到奖品，说明没有对应稀有度的奖品
        if (prize == null) {
            throw new NoPrizeAvailableException("无法抽取奖品，可能是奖池为空或没有符合条件的奖品");
        }
        
        // 扣除计划点
        state.consumePlanPoints(DRAW_COST, "通过抽卡消耗，奖品: " + prize.getName());
        return prize;
    }

    /**
     * 确定本次抽奖的稀有度结果
     * @param lotteryState 当前抽奖状态
     * @return 抽到的稀有度 (3, 4, 或 5)
     */
    public int determineRarity(LotteryState lotteryState) {
        // 检查计划点数是否足够
        if (lotteryState.getCurrentPlanPoints() < DRAW_COST) {
            throw new InsufficientPlanPointsException("计划点数不足，需要至少" + DRAW_COST + "点才能抽奖");
        }
        
        // 消耗计划点数
        lotteryState.consumePlanPoints(DRAW_COST, "抽奖消耗");
        
        // 执行抽奖逻辑，确定稀有度
        int noFourStar = lotteryState.getNoFourStarCount();
        int noFiveStar = lotteryState.getNoFiveStarCount();
        
        double fourStarRate = 0.051; // 基础4星概率5.1%
        double fiveStarRate = 0.006; // 基础5星概率0.6%
        
        // 增加4星概率
        if (noFourStar >= 8) {
            fourStarRate = 0.561; // 9抽没有4星，第10抽4星概率提升至56.1%
        }
        
        // 增加5星概率
        if (noFiveStar >= 73) {
            fiveStarRate = 0.006 + 0.06 * (noFiveStar - 72); // 从第74抽开始，每抽增加6%概率
        }
        
        // 生成随机数决定结果
        double rand = Math.random();
        
        int result;
        if (rand < fiveStarRate) {
            result = 5; // 抽中5星
            lotteryState.resetNoFiveStarCount();
            lotteryState.incrementNoFourStarCount();
        } else if (rand < fiveStarRate + fourStarRate) {
            result = 4; // 抽中4星
            lotteryState.incrementNoFiveStarCount();
            lotteryState.resetNoFourStarCount();
        } else {
            result = 3; // 抽中3星
            lotteryState.incrementNoFiveStarCount();
            lotteryState.incrementNoFourStarCount();
        }
        
        // 十抽保底机制
        if (noFourStar == 9 && result == 3) {
            result = 4; // 连续9抽没有4星或5星，则第10抽必定是4星或以上
            lotteryState.resetNoFourStarCount();
        }
        
        return result;
    }
    
    // 自定义异常类
    public static class InsufficientPlanPointsException extends RuntimeException {
        public InsufficientPlanPointsException(String message) {
            super(message);
        }
    }
    
    public static class NoPrizeAvailableException extends RuntimeException {
        public NoPrizeAvailableException(String message) {
            super(message);
        }
    }
}