package kz.nutrifit.nutrition.dto;

import lombok.Data;

@Data
public class NutritionGoalRequest {
    private Double dailyCalories;
    private Double proteinTarget;
    private Double carbsTarget;
    private Double fatTarget;
}
