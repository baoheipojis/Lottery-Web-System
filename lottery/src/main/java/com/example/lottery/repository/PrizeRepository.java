package com.example.lottery.repository;

import com.example.lottery.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrizeRepository extends JpaRepository<Prize, Long> {

    // 按稀有度查询奖品
    List<Prize> findByRarity(int rarity);

    // 按稀有度和五星类型查询奖品
    List<Prize> findByRarityAndFiveStarType(int rarity, String fiveStarType);
}
