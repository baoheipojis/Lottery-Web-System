package com.example.lottery.service;

import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.repository.LotteryHistoryRepository;
// ...existing imports...

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class LotteryService {
    
    private final LotteryHistoryRepository lotteryHistoryRepository;
    // ...existing fields...
    
    @Autowired
    public LotteryService(LotteryHistoryRepository lotteryHistoryRepository) {
        this.lotteryHistoryRepository = lotteryHistoryRepository;
        // Initialize other fields here if needed
    }
    
    /**
     * 获取所有抽奖历史记录，按照抽取时间降序排序（最新的在前）
     * @return 抽奖历史记录列表
     */
    public List<LotteryHistory> getHistory() {
        // Repository findAll method is now configured to return records in DESC order
        return lotteryHistoryRepository.findAll();
    }
    
    /**
     * 更新抽奖历史记录的兑现状态
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateRedeemStatus(Long id, boolean redeemed) {
        try {
            LotteryHistory history = lotteryHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("抽奖记录不存在"));
            
            history.setRedeemed(redeemed);
            lotteryHistoryRepository.save(history);
            
            String statusText = redeemed ? "已兑现" : "未兑现";
            return ResponseEntity.ok(Map.of(
                "message", "兑现状态更新为" + statusText + "成功",
                "status", "success"
            ));
        } catch (Exception e) {
            throw new RuntimeException("更新失败: " + e.getMessage());
        }
    }
}
