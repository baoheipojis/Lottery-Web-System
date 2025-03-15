package com.example.lottery.service;

import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import com.example.lottery.LotteryContext;
import com.example.lottery.LotteryState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class PrizeService {

    private final PrizeRepository prizeRepository;
    private final Random random = new Random();
    private final LotteryContext lotteryContext;
    private final LotteryState lotteryState;

    @Autowired
    public PrizeService(PrizeRepository prizeRepository, LotteryContext lotteryContext, LotteryState lotteryState) {
        this.prizeRepository = prizeRepository;
        this.lotteryContext = lotteryContext;
        this.lotteryState = lotteryState;
    }

    public List<Prize> getAllPrizes() {
        return prizeRepository.findAll();
    }

    public Prize getRandomPrizeByRarity(int rarity) {
        List<Prize> prizes = prizeRepository.findByRarity(rarity);
        if (prizes.isEmpty()) {
            throw new IllegalArgumentException("没有稀有度为 " + rarity + " 的奖品");
        }
        return prizes.get(random.nextInt(prizes.size()));
    }

    public Prize getRandomPrizeByRarityAndType(int rarity, String fiveStarType) {
        List<Prize> prizes = prizeRepository.findByRarityAndFiveStarType(rarity, fiveStarType);
        if (prizes.isEmpty()) {
            throw new IllegalArgumentException("没有稀有度为 " + rarity + " 和类型为 " + fiveStarType + " 的奖品");
        }
        return prizes.get(random.nextInt(prizes.size()));
    }

    public Prize savePrize(Prize prize) {
        return prizeRepository.save(prize);
    }

    public Prize drawPrize() {
        Prize prize = lotteryContext.executeDraw(lotteryState);
        if (prize != null && !prize.getIsRepeatable()) {
            prizeRepository.delete(prize);
        }
        return prize;
    }
}