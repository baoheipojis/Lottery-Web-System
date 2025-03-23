package com.example.lottery.strategy;

import com.example.lottery.LotteryState;
import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component("defaultLotteryStrategy")
public class DefaultLotteryStrategy implements LotteryStrategy {
    private static final double BASE_FOUR_STAR_PROB = 0.051;
    private static final double BASE_FIVE_STAR_PROB = 0.006;
    private static final int FIVE_STAR_GUARANTEE = 73;
    private static final int FOUR_STAR_GUARANTEE = 8;

    private final Random random = new Random();
    private final PrizeRepository prizeRepository;

    @Autowired
    public DefaultLotteryStrategy(PrizeRepository prizeRepository) {
        this.prizeRepository = prizeRepository;
    }

    @Override
    public Prize draw(LotteryState state) {
        state.incrementTotalDraws();
        state.incrementSinceLastFourStar();
        state.incrementSinceLastFiveStar();

 
        double fourStarProb = (state.getSinceLastFourStar() >= FOUR_STAR_GUARANTEE) ? 0.561 : BASE_FOUR_STAR_PROB;
        double fiveStarProb = (state.getSinceLastFiveStar() >= FIVE_STAR_GUARANTEE)
                ? BASE_FIVE_STAR_PROB + 0.06 * (state.getSinceLastFiveStar() - FIVE_STAR_GUARANTEE + 1)
                : BASE_FIVE_STAR_PROB;

        double roll = random.nextDouble();
        if (roll < fiveStarProb) {
            state.resetSinceLastFiveStar();
            state.resetSinceLastFourStar();
            return handleFiveStar(state);
        } else if (roll < fiveStarProb + fourStarProb || state.getSinceLastFourStar() >= 9) {
            state.resetSinceLastFourStar();
            return getRandomPrizeByRarity(4);
        } else {
            return getRandomPrizeByRarity(3);
        }
    }

    private Prize handleFiveStar(LotteryState state) {
        if (state.getLastFiveStar() == LotteryState.FiveStarType.NORMAL) {
            state.setLastFiveStar(LotteryState.FiveStarType.LIMITED);
            return getRandomPrizeByRarityAndType(5, "limited");
        } else {
            boolean isLimited = random.nextBoolean();
            state.setLastFiveStar(isLimited ? LotteryState.FiveStarType.LIMITED : LotteryState.FiveStarType.NORMAL);
            return isLimited ? getRandomPrizeByRarityAndType(5, "limited")
                    : getRandomPrizeByRarityAndType(5, "normal");
        }
    }

    private Prize getRandomPrizeByRarity(int rarity) {
        var prizes = prizeRepository.findByRarity(rarity);
        if (prizes.isEmpty()) {
            throw new IllegalStateException("No prizes available for the draw.");
        }
        return prizes.get(random.nextInt(prizes.size()));
    }

    private Prize getRandomPrizeByRarityAndType(int rarity, String type) {
        var prizes = prizeRepository.findByRarityAndFiveStarType(rarity, type);
        if (prizes.isEmpty()) {
            throw new IllegalStateException("No prizes available for the draw.");
        }
        return prizes.get(random.nextInt(prizes.size()));
    }
}