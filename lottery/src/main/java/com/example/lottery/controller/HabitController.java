package com.example.lottery.controller;

import com.example.lottery.entity.Habit;
import com.example.lottery.service.HabitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
public class HabitController {
    private final HabitService habitService;
    
    @Autowired
    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }
    
    @GetMapping
    public List<Habit> getAllHabits() {
        return habitService.getAllHabits();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabitById(@PathVariable Long id) {
        return habitService.getHabitById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Habit createHabit(@RequestBody Habit habit) {
        return habitService.saveHabit(habit);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable Long id, @RequestBody Habit habit) {
        habit.setId(id);
        try {
            Habit updatedHabit = habitService.updateHabit(habit);
            return ResponseEntity.ok(updatedHabit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<Habit> markHabitAsCompleted(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Habit completedHabit = habitService.markHabitAsCompleted(id, date);
            return ResponseEntity.ok(completedHabit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/uncomplete")
    public ResponseEntity<Habit> unmarkHabitCompletion(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Habit habit = habitService.unmarkHabitCompletion(id, date);
            return ResponseEntity.ok(habit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteHabit(@PathVariable Long id) {
        try {
            habitService.deleteHabit(id);
            return ResponseEntity.ok(Map.of("message", "习惯已删除", "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage(), "status", "error")
            );
        }
    }
    
    /**
     * 手动执行惩罚
     */
    @PostMapping("/{id}/apply-penalty")
    public ResponseEntity<Map<String, String>> applyPenalty(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            habitService.manualCheckAndApplyPenalty(id, date);
            return ResponseEntity.ok(Map.of(
                "message", "惩罚已成功应用", 
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(), 
                "status", "error"
            ));
        }
    }
}
