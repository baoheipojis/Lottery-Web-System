package com.example.lottery.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_points_record")
public class PlanPointsRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int amountChange;
    private LocalDateTime timestamp;
    private int balanceAfterOperation;
    private String description;

    public int getAmountChange() {
        return amountChange;
    }

    public void setAmountChange(int amountChange) {
        this.amountChange = amountChange;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getBalanceAfterOperation() {
        return balanceAfterOperation;
    }

    public void setBalanceAfterOperation(int balanceAfterOperation) {
        this.balanceAfterOperation = balanceAfterOperation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}