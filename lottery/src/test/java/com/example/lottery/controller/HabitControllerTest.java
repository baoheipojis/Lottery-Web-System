package com.example.lottery.controller;

import com.example.lottery.entity.Habit;
import com.example.lottery.service.HabitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitController.class)
public class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabitService habitService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Habit testHabit;

    @BeforeEach
    public void setup() {
        testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setName("Test Habit");
        testHabit.setBaseRewardPoints(30);
        
        when(habitService.getAllHabits()).thenReturn(Arrays.asList(testHabit));
        when(habitService.getHabitById(1L)).thenReturn(Optional.of(testHabit));
        when(habitService.saveHabit(any(Habit.class))).thenReturn(testHabit);
        when(habitService.updateHabit(any(Habit.class))).thenReturn(testHabit);
        when(habitService.markHabitAsCompleted(eq(1L), any(LocalDate.class))).thenReturn(testHabit);
        when(habitService.unmarkHabitCompletion(eq(1L), any(LocalDate.class))).thenReturn(testHabit);
    }

    @Test
    public void testGetAllHabits() throws Exception {
        mockMvc.perform(get("/api/habits"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name").value("Test Habit"));
    }

    @Test
    public void testCreateHabit() throws Exception {
        mockMvc.perform(post("/api/habits")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testHabit)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Test Habit"));
    }

    @Test
    public void testMarkHabitAsCompleted() throws Exception {
        String dateString = "2025-04-15";
        mockMvc.perform(post("/api/habits/1/complete")
               .param("date", dateString))
               .andExpect(status().isOk());
    }

    @Test
    public void testUnmarkHabitCompletion() throws Exception {
        String dateString = "2025-04-15";
        mockMvc.perform(post("/api/habits/1/uncomplete")
               .param("date", dateString))
               .andExpect(status().isOk());
    }
}
