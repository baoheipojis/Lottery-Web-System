package com.example.lottery.strategy;

import com.example.lottery.LotteryState;

// 抽奖策略接口
public interface LotteryStrategy {
    String draw(LotteryState state); // 返回抽奖结果（奖品名称或稀有度）
}
