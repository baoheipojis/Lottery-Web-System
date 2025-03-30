package com.example.lottery.controller;

import com.example.lottery.entity.Plan;
import com.example.lottery.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanService planService;
    
    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }
    
    @GetMapping
    public List<Plan> getAllPlans() {
        return planService.getAllPlans();
    }
    
    @GetMapping("/root")
    public List<Plan> getAllRootPlans() {
        return planService.getAllRootPlans();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Plan> getPlanById(@PathVariable Long id) {
        return planService.getPlanById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Plan createPlan(@RequestBody Plan plan) {
        return planService.savePlan(plan);
    }
    
    @PostMapping("/{id}/children")
    public ResponseEntity<Plan> addChildPlan(@PathVariable Long id, @RequestBody Plan childPlan) {
        try {
            Plan savedChild = planService.addChildPlan(id, childPlan);
            return ResponseEntity.ok(savedChild);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Plan> updatePlan(@PathVariable Long id, @RequestBody Plan updatedPlan) {
        return planService.getPlanById(id)
            .map(plan -> {
                plan.setTitle(updatedPlan.getTitle());
                plan.setDescription(updatedPlan.getDescription());
                plan.setExpectedCompletionTime(updatedPlan.getExpectedCompletionTime());
                plan.setRewardPoints(updatedPlan.getRewardPoints());
                return ResponseEntity.ok(planService.savePlan(plan));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<Plan> completePlan(@PathVariable Long id) {
        try {
            Plan completedPlan = planService.completePlan(id);
            return ResponseEntity.ok(completedPlan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long id) {
        try {
            planService.deletePlan(id);
            return ResponseEntity.ok(Map.of("message", "计划已删除", "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage(), "status", "error")
            );
        }
    }
}
