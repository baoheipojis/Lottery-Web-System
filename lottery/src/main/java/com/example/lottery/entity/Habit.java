package com.example.lottery.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habits")
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private int baseRewardPoints;
    
    @Column(name = "penalty_points")
    private Integer penaltyPoints;
    
    @Column(name = "enable_penalty", nullable = false)
    private boolean enablePenalty = false;
    
    @Column(name = "consecutive_days_threshold1")
    private Integer consecutiveDaysThreshold1;
    
    @Column(name = "bonus_points1")
    private Integer bonusPoints1;
    
    @Column(name = "consecutive_days_threshold2")
    private Integer consecutiveDaysThreshold2;
    
    @Column(name = "bonus_points2")
    private Integer bonusPoints2;
    
    @ElementCollection
    @CollectionTable(name = "habit_completion_dates", joinColumns = @JoinColumn(name = "habit_id"))
    @Column(name = "completion_date")
    @Convert(converter = LocalDateStringConverter.class)
    private Set<LocalDate> completionDates = new HashSet<>();
    
    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBaseRewardPoints() {
        return baseRewardPoints;
    }

    public void setBaseRewardPoints(int baseRewardPoints) {
        this.baseRewardPoints = baseRewardPoints;
    }

    public Integer getPenaltyPoints() {
        return penaltyPoints;
    }

    public void setPenaltyPoints(Integer penaltyPoints) {
        this.penaltyPoints = penaltyPoints;
    }
    
    public boolean isEnablePenalty() {
        return enablePenalty;
    }
    
    public void setEnablePenalty(boolean enablePenalty) {
        this.enablePenalty = enablePenalty;
    }

    public Integer getConsecutiveDaysThreshold1() {
        return consecutiveDaysThreshold1;
    }

    public void setConsecutiveDaysThreshold1(Integer consecutiveDaysThreshold1) {
        this.consecutiveDaysThreshold1 = consecutiveDaysThreshold1;
    }

    public Integer getBonusPoints1() {
        return bonusPoints1;
    }

    public void setBonusPoints1(Integer bonusPoints1) {
        this.bonusPoints1 = bonusPoints1;
    }

    public Integer getConsecutiveDaysThreshold2() {
        return consecutiveDaysThreshold2;
    }

    public void setConsecutiveDaysThreshold2(Integer consecutiveDaysThreshold2) {
        this.consecutiveDaysThreshold2 = consecutiveDaysThreshold2;
    }

    public Integer getBonusPoints2() {
        return bonusPoints2;
    }

    public void setBonusPoints2(Integer bonusPoints2) {
        this.bonusPoints2 = bonusPoints2;
    }

    public Set<LocalDate> getCompletionDates() {
        return completionDates;
    }

    public void setCompletionDates(Set<LocalDate> completionDates) {
        this.completionDates = completionDates;
    }

    @Converter
    public static class LocalDateStringConverter implements AttributeConverter<LocalDate, java.sql.Date> {
        @Override
        public java.sql.Date convertToDatabaseColumn(LocalDate attribute) {
            if (attribute == null) {
                return null;
            }
            // Make sure we're using the date's values, not relying on timezone conversion
            return java.sql.Date.valueOf(attribute);
        }
        
        @Override
        public LocalDate convertToEntityAttribute(java.sql.Date dbData) {
            if (dbData == null) {
                return null;
            }
            return dbData.toLocalDate();
        }
    }
}
