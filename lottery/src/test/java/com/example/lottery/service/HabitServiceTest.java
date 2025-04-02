package com.example.lottery.service;

import com.example.lottery.LotteryState;
import com.example.lottery.entity.Habit;
import com.example.lottery.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;
    
    @Mock
    private LotteryState lotteryState;
    
    @InjectMocks
    private HabitService habitService;
    
    private Habit testHabit;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setName("Test Habit");
        testHabit.setBaseRewardPoints(30);
        testHabit.setConsecutiveDaysThreshold1(3);
        testHabit.setBonusPoints1(10);
        
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
    
    @Test
    public void testMarkHabitAsCompleted() {
        // Arrange
        LocalDate testDate = LocalDate.now();
        
        // Act
        Habit result = habitService.markHabitAsCompleted(1L, testDate);
        
        // Assert
        assertTrue(result.hasCompletionDate(testDate));
        verify(lotteryState, times(1)).addPlanPoints(eq(30), anyString());
        verify(habitRepository, times(1)).save(any(Habit.class));
    }
    
    @Test
    public void testMarkHabitAsCompletedWithConsecutiveDays() {
        // Arrange
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        testHabit.addCompletionDate(yesterday);
        
        // Act
        Habit result = habitService.markHabitAsCompleted(1L, today);
        
        // Assert
        assertTrue(result.hasCompletionDate(today));
        // Should get base reward + consecutive bonus
        verify(lotteryState, times(1)).addPlanPoints(eq(30), anyString());
        verify(habitRepository, times(1)).save(any(Habit.class));
    }
    
    @Test
    public void testUnmarkHabitCompletion() {
        // Arrange
        LocalDate testDate = LocalDate.now();
        testHabit.addCompletionDate(testDate);
        
        // Act
        Habit result = habitService.unmarkHabitCompletion(1L, testDate);
        
        // Assert
        assertFalse(result.hasCompletionDate(testDate));
        verify(lotteryState, times(1)).consumePlanPoints(eq(30), anyString());
        verify(habitRepository, times(1)).save(any(Habit.class));
    }
}
