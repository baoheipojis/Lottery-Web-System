package com.example.lottery.controller;

import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.repository.LotteryHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lottery-history")
public class LotteryHistoryController {

    private final LotteryHistoryRepository lotteryHistoryRepository;

    @Autowired
    public LotteryHistoryController(LotteryHistoryRepository lotteryHistoryRepository) {
        this.lotteryHistoryRepository = lotteryHistoryRepository;
    }

    @GetMapping
    public List<LotteryHistory> getAllHistory() {
        return lotteryHistoryRepository.findAll();
    }
}