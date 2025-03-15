package com.example.lottery.strategy;

import com.example.lottery.LotteryState;
import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultLotteryStrategyTest {

    private DefaultLotteryStrategy strategy;
    private LotteryState state;
    private PrizeRepository prizeRepository;

    @BeforeEach
    void setUp() {
        prizeRepository = Mockito.mock(PrizeRepository.class);
        strategy = new DefaultLotteryStrategy(prizeRepository);
        state = new LotteryState();

        // Mock prize data
        List<Prize> threeStarPrizes = List.of(new Prize("三星奖品", 3, null, "三星奖品描述"));
        List<Prize> fourStarPrizes = List.of(new Prize("四星奖品", 4, null, "四星奖品描述"));
        List<Prize> normalFiveStarPrizes = List.of(new Prize("普通五星奖品", 5, "normal", "普通五星奖品描述"));
        List<Prize> limitedFiveStarPrizes = List.of(new Prize("限定五星奖品", 5, "limited", "限定五星奖品描述"));

        Mockito.when(prizeRepository.findByRarity(3)).thenReturn(threeStarPrizes);
        Mockito.when(prizeRepository.findByRarity(4)).thenReturn(fourStarPrizes);
        Mockito.when(prizeRepository.findByRarityAndFiveStarType(5, "normal")).thenReturn(normalFiveStarPrizes);
        Mockito.when(prizeRepository.findByRarityAndFiveStarType(5, "limited")).thenReturn(limitedFiveStarPrizes);
    }

    @Test
    void testThreeStarProbability() {
        int threeStarCount = 0;
        for (int i = 0; i < 1000; i++) {
            Prize result = strategy.draw(state);
            if (result.getRarity() == 3) {
                threeStarCount++;
            }
        }
        double actualProbability = (double) threeStarCount / 1000;
        assertTrue(actualProbability > 0.8 && actualProbability < 0.94, "三星概率验证失败");
    }

    @Test
    void testFourStarGuaranteeAfterNineThreeStars() {
        int threeStarCount = 0;
        for (int drawCount = 1; drawCount <= 5000; drawCount++) {
            Prize result = strategy.draw(state);
            if (result.getRarity() == 3) {
                threeStarCount++;
            } else {
                threeStarCount = 0;
            }
            if (threeStarCount == 9) {
                result = strategy.draw(state);
                assertTrue(
                        result.getRarity() == 4 || result.getRarity() == 5,
                        "连续9次三星后，出现了不合法的奖励：" + result.getRarity()
                );
                break;
            }
        }
    }

    @Test
    void testFourAndFiveStarProbabilities() {
        int totalDraws = 1000000;
        int fourStarCount = 0;
        int fiveStarCount = 0;

        for (int i = 0; i < totalDraws; i++) {
            Prize result = strategy.draw(state);
            if (result.getRarity() == 4) {
                fourStarCount++;
            } else if (result.getRarity() == 5) {
                fiveStarCount++;
            }
        }

        double fourStarProbability = (double) fourStarCount / totalDraws;
        double fiveStarProbability = (double) fiveStarCount / totalDraws;

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
        int totalDraws = 1000000;
        int sinceLastFiveStar = 0;

        for (int i = 1; i <= totalDraws; i++) {
            Prize result = strategy.draw(state);
            if (result.getRarity() == 5) {
                assertTrue(
                        sinceLastFiveStar <= 89,
                        "两个五星之间的间隔超过了 89 抽！间隔为：" + sinceLastFiveStar
                );
                sinceLastFiveStar = 0;
            } else {
                sinceLastFiveStar++;
            }
        }
    }

    @Test
    void testLimitedAndNormalFiveStarGuarantee() {
        int requiredFiveStarCount = 50;
        int currentFiveStarCount = 0;
        List<LotteryState.FiveStarType> fiveStarSequence = new ArrayList<>();

        int iterations = 0;
        int maxIterations = 10000;
        while (currentFiveStarCount < requiredFiveStarCount && iterations < maxIterations) {
            Prize result = strategy.draw(state);
            if (result.getRarity() == 5) {
                fiveStarSequence.add(state.getLastFiveStar());
                currentFiveStarCount++;
            }
            iterations++;
        }

        assertTrue(currentFiveStarCount >= requiredFiveStarCount,
                "抽奖次数过多仍未获得足够的五星记录");

        for (int i = 1; i < fiveStarSequence.size(); i++) {
            if (fiveStarSequence.get(i - 1) == LotteryState.FiveStarType.NORMAL) {
                assertEquals(LotteryState.FiveStarType.LIMITED, fiveStarSequence.get(i),
                        "规则失败：上一次五星为普通五星，但下一次五星未必定为限定五星");
            }
        }

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

        System.out.println("五星序列: " + fiveStarSequence);
    }
}