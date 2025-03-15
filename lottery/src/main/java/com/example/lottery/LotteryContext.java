// src/main/java/com/example/lottery/LotteryContext.java
package com.example.lottery;

import com.example.lottery.entity.Prize;
import com.example.lottery.strategy.LotteryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LotteryContext {
    private LotteryStrategy strategy;

    @Autowired
    public LotteryContext(LotteryStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(LotteryStrategy strategy) {
        this.strategy = strategy;
    }

    public Prize executeDraw(LotteryState state) {
        if (strategy == null) {
            throw new IllegalStateException("抽奖策略未设置！");
        }
        return strategy.draw(state);
    }
}