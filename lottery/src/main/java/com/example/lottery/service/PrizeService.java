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


    public Prize savePrize(Prize prize) {
        return prizeRepository.save(prize);
    }

// src/main/java/com/example/lottery/service/PrizeService.java
public Prize drawPrize() {
    Prize prize = lotteryContext.executeDraw(lotteryState);
    if (prize != null) {
        prizeRepository.save(prize); // Ensure the prize is saved
        if (!prize.getIsRepeatable()) {
            prizeRepository.delete(prize);
        }
    }
    return prize;
}
}