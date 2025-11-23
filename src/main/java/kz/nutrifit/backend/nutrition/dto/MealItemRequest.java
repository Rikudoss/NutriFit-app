package kz.nutrifit.backend.nutrition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MealItemRequest {
    @NotBlank
    private String name;
    @NotNull
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double quantity;
}
