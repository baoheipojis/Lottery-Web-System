// src/main/java/com/example/lottery/service/PrizeService.java
package com.example.lottery.service;

import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.entity.Prize;
import com.example.lottery.repository.LotteryHistoryRepository;
import com.example.lottery.repository.PrizeRepository;
import com.example.lottery.LotteryContext;
import com.example.lottery.LotteryState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PrizeService {

    private final PrizeRepository prizeRepository;
    private final LotteryHistoryRepository lotteryHistoryRepository;
    private final LotteryContext lotteryContext;
    private final LotteryState lotteryState;

    @Autowired
    public PrizeService(PrizeRepository prizeRepository, LotteryHistoryRepository lotteryHistoryRepository, LotteryContext lotteryContext, LotteryState lotteryState) {
        this.prizeRepository = prizeRepository;
        this.lotteryHistoryRepository = lotteryHistoryRepository;
        this.lotteryContext = lotteryContext;
        this.lotteryState = lotteryState;
    }

    public List<Prize> getAllPrizes() {
        return prizeRepository.findAll();
    }

    public Prize savePrize(Prize prize) {
        return prizeRepository.save(prize);
    }

    public Prize drawPrize() {
        Prize prize = lotteryContext.executeDraw(lotteryState);
        if (prize != null) {
            if (!prize.getIsRepeatable()) {
                prizeRepository.delete(prize);
            }
            // Save draw history
            LotteryHistory history = new LotteryHistory();
            LocalDateTime now = LocalDateTime.now();
            ZonedDateTime beijingTime = now.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
            history.setDrawTime(beijingTime);
            history.setPrizeName(prize.getName());
            history.setRarity(prize.getRarity());
            history.setFiveStarType(prize.getRarity() == 5 ? prize.getFiveStarType() : null);
            history.setResult("Success"); // Set the result field
            history.setPrizeId(prize.getId()); // Set the prizeId field

            lotteryHistoryRepository.save(history);
        }
        return prize;
    }

    public void deletePrize(Long id) {
        prizeRepository.deleteById(id);
    }
}