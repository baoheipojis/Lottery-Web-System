package com.example.lottery.controller;

import com.example.lottery.LotteryState;
import com.example.lottery.repository.PlanPointsRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/plan-points")
public class PlanPointsController {
    private final PlanPointsRecordRepository planPointsRecordRepository;
    private final LotteryState lotteryState;

    @Autowired
    public PlanPointsController(
        PlanPointsRecordRepository planPointsRecordRepository,
        LotteryState lotteryState
    ) {
        this.planPointsRecordRepository = planPointsRecordRepository;
        this.lotteryState = lotteryState;
    }

    @GetMapping
    public Map<String, Object> getPlanPoints() {
        int currentPoints = lotteryState.getCurrentPlanPoints();
        return Map.of(
            "currentPlanPoints", currentPoints,
            "planPointsRecords", planPointsRecordRepository.findAll()
        );
    }
    
    @GetMapping("/current")
    public int getCurrentPoints() {
        return lotteryState.getCurrentPlanPoints();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAllPlanPointsRecords() {
        try {
            long count = planPointsRecordRepository.count();
            planPointsRecordRepository.deleteAll();
            return ResponseEntity.ok(Map.of(
                "message", "成功删除 " + count + " 条计划点记录",
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "删除失败: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
}