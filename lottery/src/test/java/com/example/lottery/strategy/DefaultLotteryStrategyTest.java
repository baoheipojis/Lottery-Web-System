package com.example.lottery.strategy;

import com.example.lottery.LotteryState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        // 模拟连续 73 次未抽到五星，从第 74 次开始提高五星概率
        for (int i = 0; i < 73; i++) {
            assertNotEquals("五星奖品", strategy.draw(state), "五星不应在前73抽中出现");
        }
        boolean fiveStarFound = false;
        for (int i = 74; i <= 100; i++) { // 检查 74 到 100 抽间是否抽中五星
            if ("五星奖品".equals(strategy.draw(state))) {
                fiveStarFound = true;
                break;
            }
        }
        assertTrue(fiveStarFound, "74 抽后未触发五星保底逻辑");
    }

    @Test
    void testTenDrawGuarantee() {
        // 检查是否每 10 抽保证出一个四星及以上奖励
        for (int i = 1; i <= 100; i++) {
            String result = strategy.draw(state);
            if (i % 10 == 0) {
                assertTrue(result.equals("四星奖品") || result.equals("五星奖品"), "第 " + i + " 抽未触发保底机制");
            }
        }
    }

    @Test
    void testLimitedFiveStarLogic() {
        // 验证限定五星和普通五星交替逻辑
        state.setLastFiveStar(LotteryState.FiveStarType.NORMAL); // 上一次为普通五星
        String firstFiveStar = strategy.draw(state);
        while (!"五星奖品".equals(firstFiveStar)) {
            firstFiveStar = strategy.draw(state);
        }
        assertEquals("限定五星奖品", firstFiveStar, "普通五星后应抽中限定五星");

        String secondFiveStar = strategy.draw(state);
        while (!"五星奖品".equals(secondFiveStar)) {
            secondFiveStar = strategy.draw(state);
        }
        // 验证是否出现 50% 概率歪
        assertTrue(secondFiveStar.equals("限定五星奖品") || secondFiveStar.equals("普通五星奖品"), "限定五星后五星类型验证失败");
    }
}
