package com.example.lottery.controller;

import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.repository.LotteryHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAllHistory() {
        try {
            long count = lotteryHistoryRepository.count();
            lotteryHistoryRepository.deleteAll();
            return ResponseEntity.ok(Map.of(
                "message", "成功删除 " + count + " 条抽奖记录",
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "删除失败: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
    
    @PutMapping("/{id}/redeem")
    public ResponseEntity<Map<String, String>> updateRedeemStatus(@PathVariable Long id) {
        try {
            LotteryHistory history = lotteryHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("抽奖记录不存在"));
            
            history.setRedeemed(true);
            lotteryHistoryRepository.save(history);
            
            return ResponseEntity.ok(Map.of(
                "message", "兑现状态更新成功",
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "更新失败: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
}