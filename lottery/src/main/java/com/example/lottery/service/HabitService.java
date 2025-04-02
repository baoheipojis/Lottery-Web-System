package com.example.lottery.service;

import com.example.lottery.LotteryState;
import com.example.lottery.entity.Habit;
import com.example.lottery.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HabitService {
    private final HabitRepository habitRepository;
    private final LotteryState lotteryState;
    
    @Autowired
    public HabitService(HabitRepository habitRepository, LotteryState lotteryState) {
        this.habitRepository = habitRepository;
        this.lotteryState = lotteryState;
    }
    
    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }
    
    public Optional<Habit> getHabitById(Long id) {
        return habitRepository.findById(id);
    }
    
    public Habit saveHabit(Habit habit) {
        return habitRepository.save(habit);
    }
    
    @Transactional
    public Habit markHabitAsCompleted(Long id, LocalDate date) {
        Habit habit = habitRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("习惯未找到"));
        
        // 如果已经标记为完成，则直接返回
        if (habit.getCompletionDates().contains(date)) {
            return habit;
        }
        
        // 添加完成日期
        habit.getCompletionDates().add(date);
        
        // 计算连续天数
        int consecutiveDays = calculateConsecutiveDays(habit, date);
        
        // 根据连续天数计算奖励
        int rewardPoints = calculateRewardPoints(habit, consecutiveDays);
        
        // 发放奖励
        lotteryState.addPlanPoints(rewardPoints, 
            "完成习惯【" + habit.getName() + "】获得奖励 (连续" + consecutiveDays + "天)");
        
        return habitRepository.save(habit);
    }
    
    @Transactional
    public Habit unmarkHabitCompletion(Long id, LocalDate date) {
        Habit habit = habitRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("习惯未找到"));
        
        // 如果未标记为完成，则直接返回
        if (!habit.getCompletionDates().contains(date)) {
            return habit;
        }
        
        // 移除完成日期
        habit.getCompletionDates().remove(date);
        
        // 需要扣除奖励点数，这里简单处理为扣除基础奖励
        lotteryState.consumePlanPoints(habit.getBaseRewardPoints(), 
            "取消完成习惯【" + habit.getName() + "】，扣除奖励");
        
        return habitRepository.save(habit);
    }
    
    private int calculateConsecutiveDays(Habit habit, LocalDate currentDate) {
        int consecutiveDays = 1; // 当前日期算一天
        LocalDate checkDate = currentDate.minusDays(1);
        
        while (habit.getCompletionDates().contains(checkDate)) {
            consecutiveDays++;
            checkDate = checkDate.minusDays(1);
        }
        
        return consecutiveDays;
    }
    
    private int calculateRewardPoints(Habit habit, int consecutiveDays) {
        int rewardPoints = habit.getBaseRewardPoints();
        
        // 检查是否达到阈值2
        if (habit.getConsecutiveDaysThreshold2() != null && 
            consecutiveDays >= habit.getConsecutiveDaysThreshold2() && 
            habit.getBonusPoints2() != null) {
            rewardPoints += habit.getBonusPoints2();
        }
        // 检查是否达到阈值1
        else if (habit.getConsecutiveDaysThreshold1() != null && 
                 consecutiveDays >= habit.getConsecutiveDaysThreshold1() && 
                 habit.getBonusPoints1() != null) {
            rewardPoints += habit.getBonusPoints1();
        }
        
        return rewardPoints;
    }
    
    @Transactional
    public void deleteHabit(Long id) {
        habitRepository.deleteById(id);
    }
    
    @Transactional
    public Habit updateHabit(Habit updatedHabit) {
        Habit existingHabit = habitRepository.findById(updatedHabit.getId())
            .orElseThrow(() -> new RuntimeException("习惯不存在"));
        
        // 更新基本属性
        if (updatedHabit.getName() != null) {
            existingHabit.setName(updatedHabit.getName());
        }
        
        if (updatedHabit.getDescription() != null) {
            existingHabit.setDescription(updatedHabit.getDescription());
        }
        
        if (updatedHabit.getBaseRewardPoints() > 0) {
            existingHabit.setBaseRewardPoints(updatedHabit.getBaseRewardPoints());
        }
        
        // 更新阈值和奖励
        existingHabit.setConsecutiveDaysThreshold1(updatedHabit.getConsecutiveDaysThreshold1());
        existingHabit.setBonusPoints1(updatedHabit.getBonusPoints1());
        existingHabit.setConsecutiveDaysThreshold2(updatedHabit.getConsecutiveDaysThreshold2());
        existingHabit.setBonusPoints2(updatedHabit.getBonusPoints2());
        
        return habitRepository.save(existingHabit);
    }
}
