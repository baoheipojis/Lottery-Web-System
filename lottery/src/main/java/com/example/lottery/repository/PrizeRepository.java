package com.example.lottery.repository;

import com.example.lottery.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {
    List<Prize> findByRarity(int rarity);
    List<Prize> findByRarityAndFiveStarType(int rarity, String type);
}