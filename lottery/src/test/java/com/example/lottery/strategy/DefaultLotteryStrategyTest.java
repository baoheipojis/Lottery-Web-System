package com.example.lottery.strategy;

import com.example.lottery.LotteryState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultLotteryStrategyTest {

    private DefaultLotteryStrategy strategy;
    private LotteryState state;

    @BeforeEach
    void setUp() {
        strategy = new DefaultLotteryStrategy();
        state = new LotteryState(); // 初始化玩家状态
    }

    @Test
    void testThreeStarProbability() {
        // 模拟多次抽奖，统计三星出现的概率是否符合预期
        int threeStarCount = 0;
        for (int i = 0; i < 1000; i++) {
            String result = strategy.draw(state);
            if ("三星奖品".equals(result)) {
                threeStarCount++;
            }
        }
        // 允许一定误差范围内的三星概率验证
        double actualProbability = (double) threeStarCount / 1000;
        assertTrue(actualProbability > 0.8 && actualProbability < 0.94, "三星概率验证失败");
    }

    @Test
    void testFourStarGuaranteeAfterNineThreeStars() {
        int threeStarCount = 0; // 记录连续三星的数量

        // 模拟多次抽奖以验证逻辑
        for (int drawCount = 1; drawCount <= 5000; drawCount++) {
            String result = strategy.draw(state);

            if ("三星奖品".equals(result)) {
                threeStarCount++;
            } else {
                threeStarCount = 0; // 如果不是三星，重置计数器
            }

            if (threeStarCount == 9) {
                // 当连续9次抽中三星后，接下来的抽奖结果应是四星或五星
                result = strategy.draw(state);
                assertTrue(
                        result.equals("四星奖品") || result.equals("五星奖品"),
                        "连续9次三星后，出现了不合法的奖励：" + result
                );


                // 抽到四星后退出检测
                break;
            }
        }
    }

    @Test
    void testFourAndFiveStarProbabilities() {
        int totalDraws = 1000000; // 抽取足够多次，例如 100,000 次
        int fourStarCount = 0;  // 记录四星出现的次数
        int fiveStarCount = 0;  // 记录五星出现的次数

        for (int i = 0; i < totalDraws; i++) {
            String result = strategy.draw(state);
            if ("四星奖品".equals(result)) {
                fourStarCount++;
            } else if ("限定五星奖品".equals(result)||"普通五星奖品".equals(result)) {
                fiveStarCount++;
            }
        }

        // 计算实际概率
        double fourStarProbability = (double) fourStarCount / totalDraws;
        double fiveStarProbability = (double) fiveStarCount / totalDraws;

        // 允许一定的误差范围
        assertTrue(
                fourStarProbability >= 0.125 && fourStarProbability <= 0.135,
                "四星概率偏离预期，实际概率为：" + fourStarProbability
        );
        assertTrue(
                fiveStarProbability >= 0.015 && fiveStarProbability <= 0.017,
                "五星概率偏离预期，实际概率为：" + fiveStarProbability
        );
    }

    @Test
    void testFiveStarGuarantee() {
        int totalDraws = 1000000; // 测试 1000 次抽奖
        int sinceLastFiveStar = 0; // 记录距离上一次五星的抽数

        for (int i = 1; i <= totalDraws; i++) {
            String result = strategy.draw(state);

            if ("五星奖品".equals(result)) {
                // 每次抽到五星时检查
                assertTrue(
                        sinceLastFiveStar <= 89,
                        "两个五星之间的间隔超过了 89 抽！间隔为：" + sinceLastFiveStar
                );
                sinceLastFiveStar = 0; // 重置计数器
            } else {
                // 如果不是五星，增加计数
                sinceLastFiveStar++;
            }
        }
    }

        @Test
        void testLimitedAndNormalFiveStarGuarantee() {
            int requiredFiveStarCount = 50; // 目标抽到的五星数量，用于统计续次规则
            int currentFiveStarCount = 0;

            // 用于记录五星的类型序列
            List<LotteryState.FiveStarType> fiveStarSequence = new ArrayList<>();

            // 执行抽奖直到获得足够多的五星抽奖记录，避免无限循环设定 maxIterations
            int iterations = 0;
            int maxIterations = 10000;
            while (currentFiveStarCount < requiredFiveStarCount && iterations < maxIterations) {
                String result = strategy.draw(state);
                // 假设五星的返回字符串为 "普通五星奖品" 或 "限定五星奖品"
                if (result.equals("普通五星奖品") || result.equals("限定五星奖品")) {
                    // 记录当前五星类型（由状态中的 lastFiveStar 决定）
                    fiveStarSequence.add(state.getLastFiveStar());
                    currentFiveStarCount++;
                }
                iterations++;
            }

            // 确保至少采集到了目标数量的五星
            assertTrue(currentFiveStarCount >= requiredFiveStarCount,
                    "抽奖次数过多仍未获得足够的五星记录");

            // 规则1：如果上一次五星为普通五星，则下一次五星必定为限定五星
            for (int i = 1; i < fiveStarSequence.size(); i++) {
                if (fiveStarSequence.get(i - 1) == LotteryState.FiveStarType.NORMAL) {
                    assertEquals(LotteryState.FiveStarType.LIMITED, fiveStarSequence.get(i),
                            "规则失败：上一次五星为普通五星，但下一次五星未必定为限定五星");
                }
            }

            // 规则2：如果上一次五星为限定五星，则下一次五星的结果应有50%的概率呈现普通五星
            // 此处我们验证：在五星序列中，必须至少出现一次「限定五星后出现普通五星」的情况
            boolean observedNormalAfterLimited = false;
            for (int i = 1; i < fiveStarSequence.size(); i++) {
                if (fiveStarSequence.get(i - 1) == LotteryState.FiveStarType.LIMITED &&
                        fiveStarSequence.get(i) == LotteryState.FiveStarType.NORMAL) {
                    observedNormalAfterLimited = true;
                    break;
                }
            }
            assertTrue(observedNormalAfterLimited,
                    "规则失败：未观察到限定五星后出现普通五星的情况（50% 概率）");

            // 输出五星类型序列供人工检查（可选）
            System.out.println("五星序列: " + fiveStarSequence);
        }
    }


