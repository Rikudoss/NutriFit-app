package kz.nutrifit.nutrition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MealItemRequest {

    @NotBlank
    private String name;

    @NotNull
    private Double caloriesPer100g;

    private Double protein;
    private Double carbs;
    private Double fat;

    @NotNull
    @Positive
    private Double quantity;
}
