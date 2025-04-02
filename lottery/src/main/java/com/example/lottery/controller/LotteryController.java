package com.example.lottery.controller;

import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.service.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lottery")
public class LotteryController {
    
    private final LotteryService lotteryService;
    
    @Autowired
    public LotteryController(LotteryService lotteryService) {
        this.lotteryService = lotteryService;
    }
    
    @GetMapping("/history")
    public List<LotteryHistory> getHistory() {
        // This should return the records in newest-to-oldest order
        // Just pass through the data from the service without modification
        return lotteryService.getHistory();
    }
}