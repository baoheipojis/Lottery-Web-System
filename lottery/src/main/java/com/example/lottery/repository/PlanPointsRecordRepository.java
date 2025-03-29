package com.example.lottery.repository;

import com.example.lottery.entity.PlanPointsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlanPointsRecordRepository extends JpaRepository<PlanPointsRecord, Long> {
    @Query("SELECT COALESCE(SUM(r.amountChange), 0) FROM PlanPointsRecord r")
    int sumAllPlanPoints();
}