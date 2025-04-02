package com.example.lottery.api;

import com.example.lottery.entity.Habit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class HabitApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateHabitAndMarkAsCompleted() throws Exception {
        // 1. Create a new habit
        Habit newHabit = new Habit();
        newHabit.setName("API Test Habit");
        newHabit.setDescription("Testing habit API");
        newHabit.setBaseRewardPoints(50);
        
        MvcResult createResult = mockMvc.perform(post("/api/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newHabit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("API Test Habit"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();
        
        // Extract the created habit ID
        String responseJson = createResult.getResponse().getContentAsString();
        Habit createdHabit = objectMapper.readValue(responseJson, Habit.class);
        Long habitId = createdHabit.getId();
        
        // 2. Mark the habit as completed
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        mockMvc.perform(post("/api/habits/" + habitId + "/complete")
                .param("date", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionDates", hasItem(today)));
        
        // 3. Verify the habit was marked as completed
        mockMvc.perform(get("/api/habits/" + habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionDates", hasItem(today)));
        
        // 4. Unmark the habit completion
        mockMvc.perform(post("/api/habits/" + habitId + "/uncomplete")
                .param("date", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionDates", not(hasItem(today))));
    }

    @Test
    public void testUpdateHabit() throws Exception {
        // 1. Create a new habit
        Habit newHabit = new Habit();
        newHabit.setName("Original Habit");
        newHabit.setBaseRewardPoints(30);
        
        MvcResult createResult = mockMvc.perform(post("/api/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newHabit)))
                .andExpect(status().isOk())
                .andReturn();
        
        // Extract the created habit
        String responseJson = createResult.getResponse().getContentAsString();
        Habit createdHabit = objectMapper.readValue(responseJson, Habit.class);
        
        // 2. Update the habit
        createdHabit.setName("Updated Habit");
        createdHabit.setDescription("Updated description");
        
        mockMvc.perform(put("/api/habits/" + createdHabit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdHabit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Habit"))
                .andExpect(jsonPath("$.description").value("Updated description"));
        
        // 3. Verify the update
        mockMvc.perform(get("/api/habits/" + createdHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Habit"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }
}
