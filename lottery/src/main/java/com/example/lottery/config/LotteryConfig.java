package com.example.lottery.config;

import com.example.lottery.LotteryState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LotteryConfig {

    @Bean
    public LotteryState lotteryState() {
        return new LotteryState();
    }
}