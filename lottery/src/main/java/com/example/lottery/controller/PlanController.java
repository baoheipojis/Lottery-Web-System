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
        // 确保前端传来的repeatable等属性被正确保存
        System.out.println("Creating plan: " + plan.getTitle() + ", repeatable: " + plan.isRepeatable());
        if (plan.isRepeatable()) {
            System.out.println("Repeat type: " + plan.getRepeatType() + 
                               ", interval: " + plan.getRepeatInterval() + 
                               ", end date: " + plan.getRepeatEndDate());
        }
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
    public ResponseEntity<?> updatePlan(@PathVariable Long id, @RequestBody Plan updatedPlan) {
        try {
            // Set ID from path parameter to ensure we're updating the correct plan
            updatedPlan.setId(id);
            
            // Use service method to handle the update
            Plan result = planService.updatePlan(updatedPlan);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "message", "更新计划失败: " + e.getMessage(), 
                    "status", "error"
                )
            );
        }
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
    
    @PostMapping("/{id}/uncomplete")
    public ResponseEntity<Plan> uncompletePlan(@PathVariable Long id) {
        try {
            Plan uncompletedPlan = planService.uncompletePlan(id);
            return ResponseEntity.ok(uncompletedPlan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
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
