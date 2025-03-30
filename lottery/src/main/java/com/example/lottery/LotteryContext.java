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