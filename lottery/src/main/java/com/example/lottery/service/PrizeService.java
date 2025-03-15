package com.example.lottery.service;

import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class PrizeService {

    private final PrizeRepository prizeRepository;
    private final Random random = new Random();

    @Autowired
    public PrizeService(PrizeRepository prizeRepository) {
        this.prizeRepository = prizeRepository;
    }

    // 获取所有奖品
    public List<Prize> getAllPrizes() {
        return prizeRepository.findAll();
    }

    // 按稀有度随机获取一个奖品
    public Prize getRandomPrizeByRarity(int rarity) {
        List<Prize> prizes = prizeRepository.findByRarity(rarity);
        if (prizes.isEmpty()) {
            throw new IllegalArgumentException("没有稀有度为 " + rarity + " 的奖品");
        }
        return prizes.get(random.nextInt(prizes.size()));
    }

    // 按稀有度和五星类型随机获取一个奖品
    public Prize getRandomPrizeByRarityAndType(int rarity, String fiveStarType) {
        List<Prize> prizes = prizeRepository.findByRarityAndFiveStarType(rarity, fiveStarType);
        if (prizes.isEmpty()) {
            throw new IllegalArgumentException("没有稀有度为 " + rarity + " 和类型为 " + fiveStarType + " 的奖品");
        }
        return prizes.get(random.nextInt(prizes.size()));
    }
}
