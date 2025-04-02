package com.example.lottery.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class HabitTest {
    
    @Test
    public void testAddCompletionDate() {
        // Arrange
        Habit habit = new Habit();
        LocalDate testDate = LocalDate.of(2025, 4, 15);
        
        // Act
        habit.addCompletionDate(testDate);
        
        // Assert
        assertTrue(habit.hasCompletionDate(testDate));
        assertEquals(1, habit.getCompletionDates().size());
        assertEquals(1, habit.getCompletionDateStrings().size());
        assertTrue(habit.getCompletionDateStrings().contains("2025-04-15"));
    }
    
    @Test
    public void testRemoveCompletionDate() {
        // Arrange
        Habit habit = new Habit();
        LocalDate testDate = LocalDate.of(2025, 4, 15);
        habit.addCompletionDate(testDate);
        
        // Act
        habit.removeCompletionDate(testDate);
        
        // Assert
        assertFalse(habit.hasCompletionDate(testDate));
        assertEquals(0, habit.getCompletionDates().size());
        assertEquals(0, habit.getCompletionDateStrings().size());
    }
    
    @Test
    public void testGetCompletionDates() {
        // Arrange
        Habit habit = new Habit();
        Set<String> dateStrings = new HashSet<>();
        dateStrings.add("2025-04-15");
        dateStrings.add("2025-04-16");
        
        // Use reflection to set private field
        try {
            java.lang.reflect.Field field = Habit.class.getDeclaredField("completionDateStrings");
            field.setAccessible(true);
            field.set(habit, dateStrings);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
        
        // Act
        Set<LocalDate> dates = habit.getCompletionDates();
        
        // Assert
        assertEquals(2, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 4, 15)));
        assertTrue(dates.contains(LocalDate.of(2025, 4, 16)));
    }
}
