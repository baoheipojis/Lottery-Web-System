package com.example.lottery.repository;

import com.example.lottery.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {
    // 基本CRUD功能已由JpaRepository提供，无需额外定义
    List<Prize> findByRarityAndFiveStarType(int rarity, String fiveStarType);
    List<Prize> findByRarity(int rarity);

    // 获取所有启用的奖品
    List<Prize> findByEnabledTrue();

    // 根据稀有度和启用状态查询
    @Query("SELECT p FROM Prize p WHERE p.rarity = :rarity AND p.enabled = true")
    List<Prize> findEnabledByRarity(int rarity);

    // 根据稀有度和五星类型查询启用的奖品
    @Query("SELECT p FROM Prize p WHERE p.rarity = 5 AND p.fiveStarType = :fiveStarType AND p.enabled = true")
    List<Prize> findEnabledFiveStarsByType(String fiveStarType);
}