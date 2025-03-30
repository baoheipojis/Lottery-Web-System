package com.example.lottery.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column
    private String description;
    
    @Column(name = "expected_completion_time", nullable = false)
    private LocalDateTime expectedCompletionTime;
    
    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;
    
    @Column(name = "actual_completion_time")
    private LocalDateTime actualCompletionTime;
    
    @Column(name = "reward_points", nullable = false)
    private int rewardPoints;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference  // Prevents serializing the parent (avoiding circular reference)
    private Plan parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // Allows serializing children
    private List<Plan> children = new ArrayList<>();
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getExpectedCompletionTime() {
        return expectedCompletionTime;
    }
    
    public void setExpectedCompletionTime(LocalDateTime expectedCompletionTime) {
        this.expectedCompletionTime = expectedCompletionTime;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public LocalDateTime getActualCompletionTime() {
        return actualCompletionTime;
    }
    
    public void setActualCompletionTime(LocalDateTime actualCompletionTime) {
        this.actualCompletionTime = actualCompletionTime;
    }
    
    public int getRewardPoints() {
        return rewardPoints;
    }
    
    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
    
    public Plan getParent() {
        return parent;
    }
    
    public void setParent(Plan parent) {
        this.parent = parent;
    }
    
    public List<Plan> getChildren() {
        return children;
    }
    
    public void setChildren(List<Plan> children) {
        this.children = children;
    }
    
    // Helper method to add a child
    public void addChild(Plan child) {
        children.add(child);
        child.setParent(this);
    }
    
    // Helper method to remove a child
    public void removeChild(Plan child) {
        children.remove(child);
        child.setParent(null);
    }
}
