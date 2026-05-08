package kz.nutrifit.backend.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MealSummary {
    private String name;
    private String mealDate;
    private Double totalCalories;
    private List<MealItemSummary> items;
}
