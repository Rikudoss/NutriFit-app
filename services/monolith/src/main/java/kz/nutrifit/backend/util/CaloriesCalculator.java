package kz.nutrifit.backend.util;

import kz.nutrifit.backend.nutrition.MealItem;

import java.util.List;

public class CaloriesCalculator {
    private CaloriesCalculator() {
    }

    public static double calculateTotalCalories(List<MealItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getCaloriesPer100g() != null && item.getQuantity() != null
                        ? item.getCaloriesPer100g() * item.getQuantity() / 100.0
                        : 0.0)
                .sum();
    }
}
