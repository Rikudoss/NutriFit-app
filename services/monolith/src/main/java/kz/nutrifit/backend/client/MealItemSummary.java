package kz.nutrifit.backend.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MealItemSummary {
    private String name;
    private Double quantity;
    private Double caloriesPer100g;
}
