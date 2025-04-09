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

import jakarta.annotation.PostConstruct;
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
//        lotteryState.loadHistory();

        // 直接使用 lotteryContext 的 executeDraw 方法，不再自己判断稀有度和五星情况
        Prize selectedPrize = lotteryContext.executeDraw(lotteryState);
        
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