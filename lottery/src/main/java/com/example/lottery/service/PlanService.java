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
    
    public List<Plan> getAllRootPlans() {
        // Only return plans that don't have a parent (root plans)
        return planRepository.findByParentIsNullOrderByExpectedCompletionTimeAsc();
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
        
        // Set completed status and time
        plan.setCompleted(true);
        plan.setActualCompletionTime(LocalDateTime.now());
        
        // Award plan points
        int rewardPoints = plan.getRewardPoints();
        lotteryState.addPlanPoints(rewardPoints, "完成计划【" + plan.getTitle() + "】获得奖励");
        
        // Complete all child plans recursively
        completeChildPlans(plan);
        
        return planRepository.save(plan);
    }
    
    private void completeChildPlans(Plan parent) {
        if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        for (Plan child : parent.getChildren()) {
            if (!child.isCompleted()) {
                child.setCompleted(true);
                child.setActualCompletionTime(now);
                
                // 取消注释下面这行，使子计划奖励生效
                lotteryState.addPlanPoints(child.getRewardPoints(), "完成父计划【" + parent.getTitle() + "】时自动完成子计划【" + child.getTitle() + "】获得奖励");
                
                // Process child's children
                completeChildPlans(child);
            }
        }
    }
    
    @Transactional
    public Plan addChildPlan(Long parentId, Plan childPlan) {
        Plan parent = planRepository.findById(parentId)
            .orElseThrow(() -> new RuntimeException("父计划未找到"));
        
        childPlan.setParent(parent);
        return planRepository.save(childPlan);
    }
    
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }
}
