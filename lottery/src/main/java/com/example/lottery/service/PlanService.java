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
        
        // 添加日志以便调试
        System.out.println("Completing plan: " + plan.getTitle() + ", repeatable: " + plan.isRepeatable());
        
        // 设置完成状态和时间
        plan.setCompleted(true);
        plan.setActualCompletionTime(LocalDateTime.now());
        
        // 奖励计划点
        int rewardPoints = plan.getRewardPoints();
        lotteryState.addPlanPoints(rewardPoints, "完成计划【" + plan.getTitle() + "】获得奖励");
        
        // 完成所有子计划
        completeChildPlans(plan);
        
        // 如果是可重复的计划，创建下一个计划
        if (plan.isRepeatable()) {
            System.out.println("Creating next repeatable plan for: " + plan.getTitle());
            createNextRepeatablePlan(plan);
        }
        
        return planRepository.save(plan);
    }
    
    private void createNextRepeatablePlan(Plan completedPlan) {
        try {
            // 如果已经过了结束日期，不再创建新计划
            if (completedPlan.getRepeatEndDate() != null && 
                LocalDateTime.now().isAfter(completedPlan.getRepeatEndDate())) {
                System.out.println("End date passed, not creating new plan");
                return;
            }
            
            Plan newPlan = new Plan();
            newPlan.setTitle(completedPlan.getTitle());
            newPlan.setDescription(completedPlan.getDescription());
            newPlan.setRewardPoints(completedPlan.getRewardPoints());
            newPlan.setRepeatable(completedPlan.isRepeatable());
            newPlan.setRepeatType(completedPlan.getRepeatType());
            newPlan.setRepeatInterval(completedPlan.getRepeatInterval());
            newPlan.setRepeatEndDate(completedPlan.getRepeatEndDate());
            
            // 设置新计划的父计划
            if (completedPlan.getParent() != null) {
                newPlan.setParent(completedPlan.getParent());
            }
            
            // 根据重复类型计算下一次的预计完成时间
            LocalDateTime nextExpectedTime = calculateNextExpectedTime(
                    completedPlan.getExpectedCompletionTime(), 
                    completedPlan.getRepeatType(), 
                    completedPlan.getRepeatInterval());
            
            newPlan.setExpectedCompletionTime(nextExpectedTime);
            
            // 保存新计划
            Plan savedPlan = planRepository.save(newPlan);
            System.out.println("Created new repeatable plan: " + savedPlan.getId() + " - " + savedPlan.getTitle());
        } catch (Exception e) {
            System.err.println("Error creating next repeatable plan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private LocalDateTime calculateNextExpectedTime(LocalDateTime currentTime, 
                                                   Plan.RepeatType repeatType, 
                                                   Integer repeatInterval) {
        if (repeatInterval == null || repeatInterval <= 0) {
            repeatInterval = 1; // 默认间隔为1
        }
        
        switch (repeatType) {
            case DAILY:
                return currentTime.plusDays(repeatInterval);
            case WEEKLY:
                return currentTime.plusWeeks(repeatInterval);
            case MONTHLY:
                return currentTime.plusMonths(repeatInterval);
            default:
                return currentTime.plusDays(1); // 默认每天
        }
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
