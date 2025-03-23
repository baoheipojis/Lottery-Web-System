package com.example.lottery.service.impl;

import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import com.example.lottery.service.PrizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class PrizeServiceImpl implements PrizeService {

    private final PrizeRepository prizeRepository;
    private final Random random = new Random();

    @Autowired
    public PrizeServiceImpl(PrizeRepository prizeRepository) {
        this.prizeRepository = prizeRepository;
    }

    @Override
    public List<Prize> getAllPrizes() {
        return prizeRepository.findAll();
    }

    @Override
    public Prize drawPrize() {
        List<Prize> prizes = prizeRepository.findAll();
        if (prizes.isEmpty()) {
            throw new RuntimeException("No prizes available");
        }
        
        // 简单随机抽取实现，可以根据需求扩展为按稀有度权重抽取
        int index = random.nextInt(prizes.size());
        return prizes.get(index);
    }

    @Override
    public Prize savePrize(Prize prize) {
        // 验证奖品信息
        if (prize.getName() == null || prize.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Prize name cannot be empty");
        }
        
        if (prize.getRarity() == null) {
            throw new IllegalArgumentException("Rarity cannot be null");
        }
        
        // 只有当稀有度为5星时，才需要fiveStarType
        if (prize.getRarity() == 5 && 
            (prize.getFiveStarType() == null || prize.getFiveStarType().trim().isEmpty())) {
            prize.setFiveStarType("normal"); // 默认为普通五星
        }
        
        // 确保isRepeatable不为空
        if (prize.getIsRepeatable() == null) {
            prize.setIsRepeatable(false); // 默认为不可重复获取
        }
        
        // 保存并返回
        return prizeRepository.save(prize);
    }

    @Override
    public void deletePrize(Long id) {
        if (!prizeRepository.existsById(id)) {
            throw new RuntimeException("Prize not found with id: " + id);
        }
        prizeRepository.deleteById(id);
    }
}
