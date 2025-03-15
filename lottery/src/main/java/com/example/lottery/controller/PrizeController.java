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

    @GetMapping("/draw")
    public Prize drawPrize() {
        return prizeService.drawPrize(); // 调用 PrizeService 中的抽奖逻辑
    }
    @PostMapping
    public Prize addPrize(@RequestBody Prize prize) {
        return prizeService.savePrize(prize);
    }

}
