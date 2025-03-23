package com.example.lottery.repository;

import com.example.lottery.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {
    // 基本CRUD功能已由JpaRepository提供，无需额外定义
}