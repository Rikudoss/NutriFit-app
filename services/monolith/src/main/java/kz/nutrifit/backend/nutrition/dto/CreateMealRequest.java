package kz.nutrifit.backend.nutrition.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateMealRequest {
    @NotBlank
    private String name;

    @NotNull
    private LocalDateTime mealDate;

    @Valid
    @NotEmpty
    private List<MealItemRequest> items;
}
