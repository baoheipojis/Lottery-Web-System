package com.example.lottery.repository;

import com.example.lottery.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByCompletedOrderByExpectedCompletionTimeAsc(boolean completed);
    
    @Query("SELECT p FROM Plan p ORDER BY p.completed ASC, p.expectedCompletionTime ASC")
    List<Plan> findAllOrderByCompletedAndExpectedCompletionTime();
    
    List<Plan> findByParentIsNullOrderByExpectedCompletionTimeAsc();
}
