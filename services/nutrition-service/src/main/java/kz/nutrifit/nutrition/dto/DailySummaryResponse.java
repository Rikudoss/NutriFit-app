package kz.nutrifit.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryResponse {
    private LocalDate date;
    private double consumedCalories;
    private double consumedProtein;
    private double consumedCarbs;
    private double consumedFat;
    private int mealCount;
    private Double goalCalories;
    private Double goalProtein;
    private Double goalCarbs;
    private Double goalFat;
    private Double remainingCalories;
}
