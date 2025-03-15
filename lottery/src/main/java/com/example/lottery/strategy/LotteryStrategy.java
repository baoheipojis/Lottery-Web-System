package com.example.lottery.strategy;

import com.example.lottery.LotteryState;
import com.example.lottery.entity.Prize;

// 抽奖策略接口
public interface LotteryStrategy {
    Prize draw(LotteryState state); // Return Prize object
}
