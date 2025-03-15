package com.example.lottery.repository;

import com.example.lottery.entity.LotteryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotteryHistoryRepository extends JpaRepository<LotteryHistory, Long> {
}