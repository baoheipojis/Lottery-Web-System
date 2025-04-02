package com.example.lottery.service;

import com.example.lottery.entity.Habit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Use test profile
@Transactional // Roll back transactions after each test
@Tag("integration")
public class HabitServiceIntegrationTest {

    @Autowired
    private HabitService habitService;

    @Test
    public void testCreateAndRetrieveHabit() {
        // Create a test habit
        Habit habit = new Habit();
        habit.setName("Integration Test Habit");
        habit.setDescription("Testing habit creation and retrieval");
        habit.setBaseRewardPoints(40);
        
        // Save the habit
        Habit savedHabit = habitService.saveHabit(habit);
        
        // Retrieve all habits
        List<Habit> habits = habitService.getAllHabits();
        
        // Assert
        assertNotNull(savedHabit.getId());
        assertTrue(habits.stream().anyMatch(h -> h.getId().equals(savedHabit.getId())));
    }

    @Test
    public void testMarkAndUnmarkHabitCompletion() {
        // Create and save a test habit
        Habit habit = new Habit();
        habit.setName("Test Habit Completion");
        habit.setBaseRewardPoints(30);
        Habit savedHabit = habitService.saveHabit(habit);
        
        // Mark as completed for today
        LocalDate today = LocalDate.now();
        Habit completedHabit = habitService.markHabitAsCompleted(savedHabit.getId(), today);
        
        // Assert it was marked as completed
        assertTrue(completedHabit.hasCompletionDate(today));
        
        // Unmark completion
        Habit uncompletedHabit = habitService.unmarkHabitCompletion(savedHabit.getId(), today);
        
        // Assert it was unmarked
        assertFalse(uncompletedHabit.hasCompletionDate(today));
    }

    @Test
    public void testConsecutiveDaysStreak() {
        // Create and save a test habit
        Habit habit = new Habit();
        habit.setName("Test Consecutive Days");
        habit.setBaseRewardPoints(30);
        habit.setConsecutiveDaysThreshold1(3);
        habit.setBonusPoints1(10);
        Habit savedHabit = habitService.saveHabit(habit);
        
        // Mark as completed for consecutive days
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        
        habitService.markHabitAsCompleted(savedHabit.getId(), twoDaysAgo);
        habitService.markHabitAsCompleted(savedHabit.getId(), yesterday);
        habitService.markHabitAsCompleted(savedHabit.getId(), today);
        
        // Get the habit again
        Habit retrievedHabit = habitService.getHabitById(savedHabit.getId()).orElseThrow();
        
        // Assert all dates are marked
        assertTrue(retrievedHabit.hasCompletionDate(twoDaysAgo));
        assertTrue(retrievedHabit.hasCompletionDate(yesterday));
        assertTrue(retrievedHabit.hasCompletionDate(today));
    }
}
