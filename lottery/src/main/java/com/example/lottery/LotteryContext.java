package com.example.lottery;

import com.example.lottery.entity.Prize;
import com.example.lottery.strategy.LotteryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LotteryContext {
    private LotteryStrategy strategy;

    @Autowired
    public LotteryContext(@Autowired @Qualifier("defaultLotteryStrategy") LotteryStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(LotteryStrategy strategy) {
        this.strategy = strategy;
    }

    public Prize executeDraw(LotteryState state) {
        // 抽卡前先消耗计划点
        state.consumePlanPoints(160);
        return strategy.draw(state);
    }
}