package com.example.lottery.controller;

import com.example.lottery.entity.Prize;
import com.example.lottery.service.PrizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prizes")
public class PrizeController {

    private final PrizeService prizeService;

    @Autowired
    public PrizeController(PrizeService prizeService) {
        this.prizeService = prizeService;
    }

    // 获取所有奖品
    @GetMapping
    public List<Prize> getAllPrizes() {
        return prizeService.getAllPrizes();
    }

    // 按稀有度随机获取一个奖品
    @GetMapping("/random/{rarity}")
    public Prize getRandomPrizeByRarity(@PathVariable int rarity) {
        return prizeService.getRandomPrizeByRarity(rarity);
    }

    // 按稀有度和五星类型随机获取一个奖品
    @GetMapping("/random/{rarity}/{fiveStarType}")
    public Prize getRandomPrizeByRarityAndType(@PathVariable int rarity, @PathVariable String fiveStarType) {
        return prizeService.getRandomPrizeByRarityAndType(rarity, fiveStarType);
    }
}
