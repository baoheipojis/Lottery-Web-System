package com.example.lottery.controller;

import com.example.lottery.LotteryState;
import com.example.lottery.repository.PlanPointsRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}