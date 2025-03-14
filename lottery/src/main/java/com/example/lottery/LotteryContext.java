package com.example.lottery;

import com.example.lottery.strategy.LotteryStrategy;

// 抽奖上下文类
public class LotteryContext {
    private LotteryStrategy strategy;

    public void setStrategy(LotteryStrategy strategy) {
        this.strategy = strategy;
    }

    public String executeDraw(LotteryState state) {
        if (strategy == null) {
            throw new IllegalStateException("抽奖策略未设置！");
        }
        return strategy.draw(state);
    }
}
