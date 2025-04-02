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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class PrizeService {

    private final PrizeRepository prizeRepository;
    private final LotteryHistoryRepository lotteryHistoryRepository;
    private final LotteryContext lotteryContext;
    private final LotteryState lotteryState;
    private final Random random = new Random();

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
        // 获取抽奖结果的稀有度
        int rarityResult = lotteryContext.determineRarity(lotteryState);
        Prize lastFiveStar = lotteryHistoryRepository.findLastFiveStarPrize();
        
        // 根据稀有度和上次抽取结果，确定这次应该抽取的奖品池
        List<Prize> enabledPrizes;
        
        if (rarityResult == 5) {
            // 抽五星
            if (lastFiveStar == null || "LIMITED".equalsIgnoreCase(lastFiveStar.getFiveStarType())) {
                // 上次没有五星或者是限定五星，50%概率抽普通五星或限定五星
                boolean drawLimited = random.nextDouble() < 0.5;
                String fiveStarType = drawLimited ? "LIMITED" : "NORMAL";
                enabledPrizes = prizeRepository.findEnabledFiveStarsByType(fiveStarType);
            } else {
                // 上次是普通五星，这次必定抽限定五星
                enabledPrizes = prizeRepository.findEnabledFiveStarsByType("LIMITED");
            }
        } else {
            // 抽三星或四星
            enabledPrizes = prizeRepository.findEnabledByRarity(rarityResult);
        }
        
        // 检查是否有可用奖品
        if (enabledPrizes == null || enabledPrizes.isEmpty()) {
            throw new LotteryContext.NoPrizeAvailableException("没有可用的" + rarityResult + "星奖品");
        }
        
        // 随机选择一个启用状态的奖品
        Prize selectedPrize = enabledPrizes.get(random.nextInt(enabledPrizes.size()));
        
        // 如果奖品不可重复获取，则从数据库中删除
        if (!selectedPrize.getIsRepeatable()) {
            prizeRepository.delete(selectedPrize);
        }
        
        // 保存抽奖历史记录
        LotteryHistory history = new LotteryHistory();
        history.setDrawTimeNow();
        history.setPrizeName(selectedPrize.getName());
        history.setRarity(selectedPrize.getRarity());
        history.setFiveStarType(selectedPrize.getRarity() == 5 ? selectedPrize.getFiveStarType() : null);
        history.setResult("Success");
        history.setPrizeId(selectedPrize.getId());
        
        lotteryHistoryRepository.save(history);
        
        return selectedPrize;
    }

    @Transactional
    public Prize togglePrizeStatus(Long id, boolean enabled) {
        Prize prize = prizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("奖品不存在"));

        prize.setEnabled(enabled);
        return prizeRepository.save(prize);
    }

    public void deletePrize(Long id) {
        prizeRepository.deleteById(id);
    }
}