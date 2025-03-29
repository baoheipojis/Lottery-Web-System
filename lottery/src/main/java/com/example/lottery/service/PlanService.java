package com.example.lottery.service;

import com.example.lottery.LotteryState;
import com.example.lottery.entity.Plan;
import com.example.lottery.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PlanService {
    private final PlanRepository planRepository;
    private final LotteryState lotteryState;
    
    @Autowired
    public PlanService(PlanRepository planRepository, LotteryState lotteryState) {
        this.planRepository = planRepository;
        this.lotteryState = lotteryState;
    }
    
    public List<Plan> getAllPlans() {
        return planRepository.findAllOrderByCompletedAndExpectedCompletionTime();
    }
    
    public Optional<Plan> getPlanById(Long id) {
        return planRepository.findById(id);
    }
    
    public Plan savePlan(Plan plan) {
        return planRepository.save(plan);
    }
    
    @Transactional
    public Plan completePlan(Long id) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("计划未找到"));
        
        if (plan.isCompleted()) {
            throw new RuntimeException("计划已经完成");
        }
        
        // 设置完成状态
        plan.setCompleted(true);
        plan.setActualCompletionTime(LocalDateTime.now());
        
        // 奖励计划点
        int rewardPoints = plan.getRewardPoints();
        lotteryState.addPlanPoints(rewardPoints, "完成计划【" + plan.getTitle() + "】获得奖励");
        
        return planRepository.save(plan);
    }
    
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }
}
