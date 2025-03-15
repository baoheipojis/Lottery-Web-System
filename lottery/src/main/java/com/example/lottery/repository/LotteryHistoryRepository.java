package com.example.lottery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.lottery.entity.LotteryHistory;

public interface LotteryHistoryRepository extends JpaRepository<LotteryHistory, Long> {
    // 可以增加一些自定义查询方法，例如：
    // List<LotteryHistory> findTop10ByOrderByDrawTimeDesc();
}
