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
        return lotteryService.getHistory();
    }
    
    /**
     * 更新奖励的兑现状态
     */
    @PutMapping("/history/{id}/redeem")
    public ResponseEntity<?> updateRedeemStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        try {
            boolean redeemed = payload.getOrDefault("redeemed", true);
            LotteryHistory updatedHistory = lotteryService.updateRedeemStatus(id, redeemed);
            return ResponseEntity.ok(updatedHistory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "status", "error"
            ));
        }
    }
}