package kz.nutrifit.nutrition.controller;

import kz.nutrifit.nutrition.entity.Meal;
import kz.nutrifit.nutrition.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/nutrition/users")
@RequiredArgsConstructor
public class InternalNutritionController {

    private final MealRepository mealRepository;

    @GetMapping("/{userId}/meals")
    public ResponseEntity<List<Meal>> getRecentMeals(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "50") int limit) {
        List<Meal> meals = mealRepository.findTop50ByUserIdOrderByMealDateDesc(userId)
                .stream().limit(limit).toList();
        return ResponseEntity.ok(meals);
    }
}
