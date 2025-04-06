package com.example.lottery.service;

import com.example.lottery.LotteryState;
import com.example.lottery.entity.Habit;
import com.example.lottery.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        
        // Debug logs
        System.out.println("Original date parameter: " + date);
        System.out.println("Date as string: " + date.toString());
        
        // 使用新的检查方法
        if (habit.hasCompletionDate(date)) {
            System.out.println("Date already marked as completed: " + date);
            return habit;
        }
        
        // 使用新的添加方法
        habit.addCompletionDate(date);
        
        // Debug log - 使用getter方法访问字符串集合
        System.out.println("Dates in string set after adding:");
        for (String dateStr : habit.getCompletionDateStrings()) {  // 使用getter方法
            System.out.println("  - " + dateStr);
        }
        
        // 计算连续天数
        int consecutiveDays = calculateConsecutiveDays(habit.getCompletionDates(), date);
        
        // 根据连续天数计算奖励
        int rewardPoints = calculateRewardPoints(habit, consecutiveDays);
        
        // 发放奖励
        lotteryState.addPlanPoints(rewardPoints, 
            "完成习惯【" + habit.getName() + "】获得奖励 (连续" + consecutiveDays + "天)");
        
        Habit savedHabit = habitRepository.save(habit);
        
        // Debug after save - 使用getter方法查看字符串集合
        System.out.println("After saving - dates in string set:");
        for (String dateStr : savedHabit.getCompletionDateStrings()) {  // 使用getter方法
            System.out.println("  - " + dateStr);
        }
        
        return savedHabit;
    }
    
    @Transactional
    public Habit unmarkHabitCompletion(Long id, LocalDate date) {
        Habit habit = habitRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("习惯未找到"));
        
        // 使用新的检查方法
        if (!habit.hasCompletionDate(date)) {
            return habit;
        }
        
        // 在移除前计算奖励
        Set<LocalDate> datesCopy = new HashSet<>(habit.getCompletionDates());
        int consecutiveDays = calculateConsecutiveDays(datesCopy, date);
        int rewardPoints = calculateRewardPoints(habit, consecutiveDays);
        
        // 使用新的移除方法
        habit.removeCompletionDate(date);
        
        // 扣除全部奖励点数（基础+加成）
        lotteryState.consumePlanPoints(rewardPoints, 
            "取消完成习惯【" + habit.getName() + "】，扣除奖励 (之前连续" + consecutiveDays + "天，奖励" + rewardPoints + "点)");
        
        return habitRepository.save(habit);
    }
    
    /**
     * 计算指定日期的连续天数
     * @param completionDates 完成日期集合
     * @param currentDate 当前日期
     * @return 连续天数
     */
    private int calculateConsecutiveDays(Set<LocalDate> completionDates, LocalDate currentDate) {
        int consecutiveDays = 1; // 当前日期算一天
        LocalDate checkDate = currentDate.minusDays(1);
        
        while (completionDates.contains(checkDate)) {
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
        
        // 更新惩罚相关属性
        existingHabit.setEnablePenalty(updatedHabit.isEnablePenalty());
        if (updatedHabit.getPenaltyPoints() != null) {
            existingHabit.setPenaltyPoints(updatedHabit.getPenaltyPoints());
        }
        
        // 更新自动完成属性
        existingHabit.setAutoComplete(updatedHabit.isAutoComplete());
        
        return habitRepository.save(existingHabit);
    }
    
    /**
     * 检查昨天未完成的习惯并应用惩罚
     * 每天凌晨00:05执行
     */
    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void checkAndApplyPenalties() {
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // 获取所有习惯
        List<Habit> allHabits = habitRepository.findAll();
        
        for (Habit habit : allHabits) {
            // 只检查启用了惩罚的习惯
            if (habit.isEnablePenalty() && habit.getPenaltyPoints() != null && habit.getPenaltyPoints() > 0) {
                // 检查昨天是否完成了该习惯
                if (!habit.getCompletionDates().contains(yesterday)) {
                    // 未完成，应用惩罚
                    lotteryState.consumePlanPoints(
                        habit.getPenaltyPoints(),
                        "习惯【" + habit.getName() + "】在 " + yesterday + " 未完成，扣除惩罚点数"
                    );
                    
                    System.out.println("Applied penalty of " + habit.getPenaltyPoints() + 
                                       " points for habit '" + habit.getName() + 
                                       "' not completed on " + yesterday);
                }
            }
        }
    }
    
    /**
     * 自动标记习惯为已完成
     * 每天凌晨00:01执行
     */
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void autoCompleteHabits() {
        // 获取今天的日期
        LocalDate today = LocalDate.now();
        
        // 获取所有习惯
        List<Habit> allHabits = habitRepository.findAll();
        
        for (Habit habit : allHabits) {
            // 只处理启用了自动完成的习惯
            if (habit.isAutoComplete()) {
                // 检查今天是否已完成该习惯
                if (!habit.hasCompletionDate(today)) {
                    // 自动标记为完成
                    habit.addCompletionDate(today);
                    
                    // 计算连续天数
                    int consecutiveDays = calculateConsecutiveDays(habit.getCompletionDates(), today);
                    
                    // 根据连续天数计算奖励
                    int rewardPoints = calculateRewardPoints(habit, consecutiveDays);
                    
                    // 发放奖励
                    lotteryState.addPlanPoints(rewardPoints, 
                        "习惯【" + habit.getName() + "】自动完成获得奖励 (连续" + consecutiveDays + "天)");
                    
                    habitRepository.save(habit);
                    
                    System.out.println("Auto-completed habit '" + habit.getName() + 
                                       "' for " + today + 
                                       " with " + rewardPoints + " reward points");
                }
            }
        }
    }
    
    /**
     * 手动执行惩罚检查，用于测试或手动触发
     */
    @Transactional
    public void manualCheckAndApplyPenalty(Long habitId, LocalDate date) {
        Habit habit = habitRepository.findById(habitId)
            .orElseThrow(() -> new RuntimeException("习惯未找到"));
            
        if (habit.isEnablePenalty() && habit.getPenaltyPoints() != null && habit.getPenaltyPoints() > 0) {
            if (!habit.hasCompletionDate(date)) {
                // 未完成，应用惩罚
                lotteryState.consumePlanPoints(
                    habit.getPenaltyPoints(),
                    "习惯【" + habit.getName() + "】在 " + date + " 未完成，手动扣除惩罚点数"
                );
                
                System.out.println("Manually applied penalty of " + habit.getPenaltyPoints() + 
                                  " points for habit '" + habit.getName() + 
                                  "' not completed on " + date);
            } else {
                throw new RuntimeException("该日期已完成习惯，无需惩罚");
            }
        } else {
            throw new RuntimeException("该习惯未启用惩罚或惩罚点数无效");
        }
    }
}
