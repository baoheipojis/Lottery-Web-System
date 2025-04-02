package com.example.lottery.repository;

import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotteryHistoryRepository extends JpaRepository<LotteryHistory, Long> {
    
    // Find last drawn 5-star prize, sorted by draw time descending
    @Query(value = "SELECT * FROM lottery_history WHERE rarity = 5 ORDER BY draw_time DESC LIMIT 1", nativeQuery = true)
    LotteryHistory findLastFiveStarHistory();
    
    // This query is used in PrizeService to get the last 5-star prize
    @Query("SELECT p FROM Prize p WHERE p.id = (SELECT lh.prizeId FROM LotteryHistory lh WHERE lh.rarity = 5 ORDER BY lh.drawTime DESC LIMIT 1)")
    Prize findLastFiveStarPrize();
    
    // Override findAll to return records sorted by draw time (newest first)
    @Query("SELECT lh FROM LotteryHistory lh ORDER BY lh.drawTime DESC")
    List<LotteryHistory> findAll();
    
    // Add any other query methods you might need
    List<LotteryHistory> findByPrizeId(Long prizeId);
    
    List<LotteryHistory> findByRarity(int rarity);
}