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
    
    /**
     * 将已完成的计划标记为未完成
     */
    @Transactional
    public Plan uncompletePlan(Long id) {
        System.out.println("开始取消完成计划，ID: " + id);
        
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("计划未找到"));
        
        System.out.println("找到计划: " + plan.getTitle() + ", 已完成状态: " + plan.isCompleted());
        
        if (!plan.isCompleted()) {
            System.out.println("计划未完成，抛出异常");
            throw new RuntimeException("计划尚未完成，无需取消完成状态");
        }
        
        // 如果有父计划，记录其状态
        if (plan.getParent() != null) {
            System.out.println("父计划: " + plan.getParent().getTitle() + ", 已完成状态: " + plan.getParent().isCompleted());
        }
        
        // 设置为未完成状态
        plan.setCompleted(false);
        plan.setActualCompletionTime(null);
        
        try {
            // 扣除之前奖励的计划点
            int rewardPoints = plan.getRewardPoints();
            lotteryState.consumePlanPoints(rewardPoints, "取消完成计划【" + plan.getTitle() + "】，扣除之前奖励的计划点");
            System.out.println("已扣除计划点: " + rewardPoints);
        } catch (Exception e) {
            System.err.println("扣除计划点失败: " + e.getMessage());
            // 继续执行，允许点数为负
        }
        
        System.out.println("已将计划标记为未完成: " + plan.getTitle());
        
        // 保存并返回更新后的计划
        Plan savedPlan = planRepository.save(plan);
        System.out.println("已保存未完成的计划，ID: " + savedPlan.getId());
        
        return savedPlan;
    }
    
    private void createNextRepeatablePlan(Plan completedPlan) {
        Plan nextPlan = new Plan();
        nextPlan.setTitle(completedPlan.getTitle());
        nextPlan.setDescription(completedPlan.getDescription());
        nextPlan.setRewardPoints(completedPlan.getRewardPoints());
        nextPlan.setParent(completedPlan.getParent());
        nextPlan.setRepeatable(completedPlan.isRepeatable());
        nextPlan.setRepeatType(completedPlan.getRepeatType());
        nextPlan.setRepeatInterval(completedPlan.getRepeatInterval());
        nextPlan.setRepeatEndDate(completedPlan.getRepeatEndDate());
        nextPlan.setCompleted(false);
        nextPlan.setCompletionCount(0); // 显式设置为0
        nextPlan.setCurrentStreak(0); // 设置当前连续完成次数为0
        nextPlan.setActualCompletionTime(null); // 确保为null
        
        // 根据重复类型计算下一次的预计完成时间
        LocalDateTime nextExpectedTime = calculateNextExpectedTime(
                completedPlan.getExpectedCompletionTime(), 
                completedPlan.getRepeatType(), 
                completedPlan.getRepeatInterval());
        
        nextPlan.setExpectedCompletionTime(nextExpectedTime);
        
        // 保存新计划
        Plan savedPlan = planRepository.save(nextPlan);
        System.out.println("Created new repeatable plan: " + savedPlan.getId() + " - " + savedPlan.getTitle());
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
    
    /**
     * Updates an existing plan with new data while preserving relationships and children
     */
    @Transactional
    public Plan updatePlan(Plan updatedPlan) {
        // Fetch the existing plan to ensure it exists and get the full entity with relationships
        Plan existingPlan = planRepository.findById(updatedPlan.getId())
            .orElseThrow(() -> new RuntimeException("计划不存在，ID: " + updatedPlan.getId()));
        
        // Update basic properties - 添加 null 检查，仅当有值时才更新
        if (updatedPlan.getTitle() != null) {
            existingPlan.setTitle(updatedPlan.getTitle());
        }
        
        // 添加对描述的 null 检查，这是问题所在
        if (updatedPlan.getDescription() != null) {
            existingPlan.setDescription(updatedPlan.getDescription());
        }
        
        // Handle date fields if provided
        if (updatedPlan.getExpectedCompletionTime() != null) {
            existingPlan.setExpectedCompletionTime(updatedPlan.getExpectedCompletionTime());
        }
        
        // Update reward points if valid value provided
        if (updatedPlan.getRewardPoints() > 0) {
            existingPlan.setRewardPoints(updatedPlan.getRewardPoints());
        }
        
        // 只在明确设置了重复属性时更新
        if (updatedPlan.isRepeatable()) {
            existingPlan.setRepeatable(true);
            if (updatedPlan.getRepeatType() != null) {
                existingPlan.setRepeatType(updatedPlan.getRepeatType());
            }
            if (updatedPlan.getRepeatInterval() != null) {
                existingPlan.setRepeatInterval(updatedPlan.getRepeatInterval());
            }
            existingPlan.setRepeatEndDate(updatedPlan.getRepeatEndDate());
        } else if (updatedPlan.isRepeatable() == false) { // 明确设置为不重复
            existingPlan.setRepeatable(false);
            existingPlan.setRepeatType(null);
            existingPlan.setRepeatInterval(null);
            existingPlan.setRepeatEndDate(null);
        }
        
        // Save and return updated plan
        return planRepository.save(existingPlan);
    }
}
