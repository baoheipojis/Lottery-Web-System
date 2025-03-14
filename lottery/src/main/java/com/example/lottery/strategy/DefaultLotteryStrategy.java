package com.example.lottery.strategy;

import com.example.lottery.LotteryState;
import java.util.Random;

public class DefaultLotteryStrategy implements LotteryStrategy {
    private static final double BASE_FOUR_STAR_PROB = 0.051;
    private static final double BASE_FIVE_STAR_PROB = 0.006;
    private static final int FIVE_STAR_GUARANTEE = 73;
    private static final int FOUR_STAR_GUARANTEE = 8;

    private Random random = new Random();

    @Override
    public String draw(LotteryState state) {
        state.incrementTotalDraws();
        state.incrementSinceLastFourStar();
        state.incrementSinceLastFiveStar();

        // 动态调整概率
        double fourStarProb = (state.getSinceLastFourStar() >= FOUR_STAR_GUARANTEE) ? 0.561 : BASE_FOUR_STAR_PROB;
        double fiveStarProb = (state.getSinceLastFiveStar() >= FIVE_STAR_GUARANTEE)
                ? BASE_FIVE_STAR_PROB + 0.06 * (state.getSinceLastFiveStar() - FIVE_STAR_GUARANTEE + 1)
                : BASE_FIVE_STAR_PROB;

        double roll = random.nextDouble(); // 生成0~1的随机数

        // 判断结果
        if (roll < fiveStarProb) {
            state.resetSinceLastFiveStar();
            state.resetSinceLastFourStar();
            return handleFiveStar(state); // 调用五星处理逻辑
        } else if (roll < fiveStarProb+fourStarProb) {
            state.resetSinceLastFourStar();
            return "四星奖品";
        } else {
            return "三星奖品";
        }
    }

    private String handleFiveStar(LotteryState state) {
        // 判断上次五星的类型
        if (state.getLastFiveStar() == LotteryState.FiveStarType.NORMAL) {
            // 如果上次是普通五星，本次必定是限定五星
            state.setLastFiveStar(LotteryState.FiveStarType.LIMITED);
            return "限定五星奖品";
        } else {
            // 如果上次是限定五星，本次有 50% 概率为普通五星
            boolean isLimited = random.nextBoolean(); // 50% 概率
            state.setLastFiveStar(isLimited ? LotteryState.FiveStarType.LIMITED : LotteryState.FiveStarType.NORMAL);
            return isLimited ? "限定五星奖品" : "普通五星奖品";
        }
    }
}
