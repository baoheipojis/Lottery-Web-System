// src/main/java/com/example/lottery/config/LotteryConfig.java
package com.example.lottery.config;

import com.example.lottery.LotteryState;
import com.example.lottery.repository.LotteryHistoryRepository;
import com.example.lottery.repository.PlanPointsRecordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LotteryConfig {

    @Bean
    public LotteryState lotteryState(
            LotteryHistoryRepository lotteryHistoryRepository,
            PlanPointsRecordRepository planPointsRecordRepository
    ) {
        return new LotteryState(lotteryHistoryRepository, planPointsRecordRepository);
    }
}